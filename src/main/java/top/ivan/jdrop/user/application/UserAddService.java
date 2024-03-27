package top.ivan.jdrop.user.application;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import top.ivan.jdrop.common.JDropConfig;
import top.ivan.jdrop.common.cache.CacheNotAccessableException;
import top.ivan.jdrop.common.cache.LambdaHandler;
import top.ivan.jdrop.common.cache.LimitCache;
import top.ivan.jdrop.common.exception.IllegalClientRequestException;
import top.ivan.jdrop.common.qrcode.QrCodeWindow;
import top.ivan.jdrop.user.domain.ConnectProtocolEntity;
import top.ivan.jdrop.user.infrastructure.DateUtils;
import top.ivan.jdrop.user.infrastructure.assembler.UserConnectAssembler;
import top.ivan.jdrop.user.infrastructure.factory.UserFactory;
import top.ivan.jdrop.user.infrastructure.repo.UserRepository;
import top.ivan.jdrop.user.userinterface.module.dto.UserBindDTO;
import top.ivan.jdrop.user.userinterface.module.dto.UserConnectDTO;
import top.ivan.jdrop.user.userinterface.module.vo.UserBindVO;
import top.ivan.windrop.util.JSONUtils;

/**
 * @author Ivan
 * @since 2024/03/12 10:36
 */
@Slf4j
@Service
public class UserAddService {

    private static final UserConnectAssembler USER_CONNECT_ASSEMBLER = new UserConnectAssembler();

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private UserFactory userFactory;

    @Autowired
    private JDropConfig config;

    private final LimitCache<ConnectProtocolEntity> protocolCache = new LimitCache<>();
    private final Cache<String, QrCodeWindow> windowCache = CacheBuilder.newBuilder().maximumSize(1).build();

    public Mono<UserBindDTO> bindUser(UserBindVO bind) {
        return getProtocol(bind.getToken())
                .flatMap(p -> p.checkSign(bind) ? Mono.just(p) :
                        Mono.error(() -> new IllegalClientRequestException("核验失败，请检查网络安全")))
                .doOnNext(p -> userRepo.saveUser(userFactory.newUser(bind.getId(), bind.getName(), p)))
                .map(p -> new UserBindDTO(bind.getId(), p.getKeyElement()));
    }

    public Mono<UserConnectDTO> newUserConnect(int expireMillions) {
        return Mono.just(new ConnectProtocolEntity(expireMillions))
                .doOnNext(entity -> protocolCache.register(entity.toToken(), () -> entity, 1, expireMillions))
                .map(USER_CONNECT_ASSEMBLER::convertToUserConnect)
                .doOnNext(dto -> dto.setNetPort(config.getPort()));
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
        QrCodeWindow window = createConnectQrCodeWindow(expireMillions, handler);

        //active
        return Mono.fromRunnable(window::active)
                .then(Mono.fromSupplier(handler::getValue))
                //set description
                .doOnNext(uc -> window.setDescription(formatQrCodeDescription(expireMillions, uc.getHostName())))
                //cache window (for close window after bind)
                .doOnNext(uc -> windowCache.put(uc.getTokenElement(), window));
    }

    private QrCodeWindow createConnectQrCodeWindow(int expireMillions, LambdaHandler<UserConnectDTO> handler) {
        QrCodeWindow window = new QrCodeWindow("连接码", 55, 60,
                () -> newUserConnect(expireMillions)
                        .doOnNext(handler::setValue)
                        .map(JSONUtils::toString)
                        .toFuture().join()
        );

        //set trigger
        window.onExpire(w -> removeConnectBind(handler.getValue())
                .then(Mono.fromRunnable(w::refresh))
                .toFuture().join()
        );
        window.onClose(w -> removeConnectBind(handler.getValue())
                .toFuture().join()
        );
        return window;
    }

    private Mono<ConnectProtocolEntity> getProtocol(String token) {
        return Mono.fromSupplier(() -> protocolCache.getCacheData(token))
                .filter(LimitCache.CacheData::isAccessible)
                .map(LimitCache.CacheData::getData)
                .switchIfEmpty(Mono.error(() -> new CacheNotAccessableException("no protocol found")))
                .doOnError(e -> log.error("get protocol failed: {}", e.getMessage()));
    }

    private static String formatQrCodeDescription(int expireMillions, String hostname) {
        return String.format("连接域名: %s\n" +
                        "连接有效期: %s",
                hostname, DateUtils.secondToDateDuration(expireMillions / 1000));
    }

    public static void main(String[] args) {
        UserAddService service = new UserAddService();
        service.showConnectQrCode(1000 * 60 * 60 * 10).block();
    }
}
