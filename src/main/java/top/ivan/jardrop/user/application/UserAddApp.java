package top.ivan.jardrop.user.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import top.ivan.jardrop.common.cache.LimitCache;
import top.ivan.jardrop.qrcode.CacheNotAccessableException;
import top.ivan.jardrop.user.Entity.Protocol;
import top.ivan.jardrop.user.UserService;
import top.ivan.jardrop.user.dto.UserBindDTO;
import top.ivan.jardrop.user.vo.UserBindResponse;

/**
 * @author Ivan
 * @since 2024/03/12 10:36
 */
@Service
public class UserAddApp {

    @Autowired
    private UserService userService;

    private final LimitCache<Protocol> cache = new LimitCache<>();

    public Mono<UserBindResponse> bindUser(UserBindDTO uc) {
        return getProtocol(uc.getToken())
                .doOnNext(protocol -> userService.addUser(uc, protocol))
                .map(p -> new UserBindResponse(uc.getId(), p.getKeyElement()));
    }

    private Mono<Protocol> getProtocol(String token) {
        return Mono.fromSupplier(() -> cache.getCacheData(token))
                .filter(LimitCache.CacheData::isAccessible)
                .map(LimitCache.CacheData::getData)
                .or(Mono.error(() -> new CacheNotAccessableException("no protocol found")));
    }

}
