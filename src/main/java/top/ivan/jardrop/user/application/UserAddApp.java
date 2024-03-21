package top.ivan.jardrop.user.application;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import top.ivan.jardrop.common.cache.CacheNotAccessableException;
import top.ivan.jardrop.common.cache.LambdaHandler;
import top.ivan.jardrop.common.cache.LimitCache;
import top.ivan.jardrop.common.qrcode.QrCodeWindow;
import top.ivan.jardrop.user.application.assembler.UserConnectAssembler;
import top.ivan.jardrop.user.application.dto.UserBindDTO;
import top.ivan.jardrop.user.application.dto.UserConnectDTO;
import top.ivan.jardrop.user.domain.ConnectProtocolEntity;
import top.ivan.jardrop.user.domain.UserFactory;
import top.ivan.jardrop.user.domain.UserRepository;
import top.ivan.jardrop.user.util.DateUtils;
import top.ivan.jardrop.user.vo.UserBindResponse;
import top.ivan.windrop.util.JSONUtils;

/**
 * @author Ivan
 * @since 2024/03/12 10:36
 */
@Service
public class UserAddApp {

    private static final UserConnectAssembler USER_CONNECT_ASSEMBLER = new UserConnectAssembler();

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private UserFactory userFactory;

    private final LimitCache<ConnectProtocolEntity> protocolCache = new LimitCache<>();
    private final Cache<String, QrCodeWindow> windowCache = CacheBuilder.newBuilder().maximumSize(1).build();

    public Mono<UserBindResponse> bindUser(UserBindDTO bind) {
        return getProtocol(bind.getToken())
                .doOnNext(p -> userRepo.saveUser(userFactory.newUser(bind.getId(),
                        bind.getName(),
                        p.toValidKey(),
                        p.getExpireMillions())))
                .map(p -> new UserBindResponse(bind.getId(), p.getKeyElement()));
    }

    private Mono<ConnectProtocolEntity> getProtocol(String token) {
        return Mono.fromSupplier(() -> protocolCache.getCacheData(token))
                .filter(LimitCache.CacheData::isAccessible)
                .map(LimitCache.CacheData::getData)
                .or(Mono.error(() -> new CacheNotAccessableException("no protocol found")));
    }

    public Mono<UserConnectDTO> newUserConnect(int expireMillions) {
        return Mono.just(new ConnectProtocolEntity(expireMillions))
                .doOnNext(p -> protocolCache.register(p.toToken(), () -> p, 1, expireMillions))
                .map(USER_CONNECT_ASSEMBLER::convertToUserConnect);
    }

    public Mono<Void> removeConnectBind(UserConnectDTO uc) {
        if (null == uc) {
            return Mono.empty();
        }
        String token = ConnectProtocolEntity.toToken(uc.getKeyRoot(), uc.getTokenElement());
        return Mono.fromRunnable(() -> protocolCache.remove(token));
    }

    public Mono<UserConnectDTO> showConnectQrCode(int expireMillions) {
        //dto handler
        LambdaHandler<UserConnectDTO> handler = new LambdaHandler<>();

        //init display window
        QrCodeWindow window = new QrCodeWindow("连接码", 55, 60,
                () -> newUserConnect(expireMillions)
                        .doOnNext(handler::setValue)
                        .map(JSONUtils::toString)
                        .block());

        //set trigger
        window.onExpire(w -> removeConnectBind(handler.getValue())
                .then(Mono.fromRunnable(w::refresh)).block());
        window.onClose(w -> removeConnectBind(handler.getValue()).block());

        //active
        return Mono.fromRunnable(window::active)
                .then(Mono.fromSupplier(handler::getValue))
                //set description
                .doOnNext(uc -> window.setDescription(formatQrCodeDescription(expireMillions, uc.getHostName())))
                //cache window (for close window after bind)
                .doOnNext(uc -> windowCache.put(uc.getTokenElement(), window));
    }

    private static String formatQrCodeDescription(int expireMillions, String hostname) {
        return String.format("连接域名: %s\n" +
                        "连接有效期: %s",
                hostname, DateUtils.secondToDateDuration(expireMillions / 1000));
    }

}
