package top.ivan.jardrop.user.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import top.ivan.jardrop.common.cache.LimitCache;
import top.ivan.jardrop.common.util.NetUtils;
import top.ivan.jardrop.qrcode.CacheNotAccessableException;
import top.ivan.jardrop.user.Entity.ConnectProtocol;
import top.ivan.jardrop.user.UserService;
import top.ivan.jardrop.user.dto.UserBindDTO;
import top.ivan.jardrop.user.dto.UserConnectDTO;
import top.ivan.jardrop.user.vo.UserBindResponse;

/**
 * @author Ivan
 * @since 2024/03/12 10:36
 */
@Service
public class UserAddApp {

    @Autowired
    private UserService userService;

    private final LimitCache<ConnectProtocol> cache = new LimitCache<>();

    public Mono<UserBindResponse> bindUser(UserBindDTO uc) {
        return getProtocol(uc.getToken())
                .doOnNext(protocol -> userService.addUser(uc, protocol))
                .map(p -> new UserBindResponse(uc.getId(), p.getKeyElement()));
    }

    private Mono<ConnectProtocol> getProtocol(String token) {
        return Mono.fromSupplier(() -> cache.getCacheData(token))
                .filter(LimitCache.CacheData::isAccessible)
                .map(LimitCache.CacheData::getData)
                .or(Mono.error(() -> new CacheNotAccessableException("no protocol found")));
    }

    public Mono<UserConnectDTO> newUserConnect(int expireMillions) {
        ConnectProtocol protocol = new ConnectProtocol(expireMillions);
        cache.register(protocol.toToken(), () -> protocol, 1, expireMillions);

        return Mono.just(new UserConnectDTO())
                .doOnNext(uc -> uc.setKeyRoot(protocol.getKeyRoot()))
                .doOnNext(uc -> uc.setTokenElement(protocol.getTokenElement()))
                .doOnNext(uc -> uc.setHostName(NetUtils.getLocalHostName()));
    }

    public Mono<Void> newConnectQrCode(int expireMillions) {
        return Mono.empty();
    }
}
