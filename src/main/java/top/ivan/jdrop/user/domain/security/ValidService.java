package top.ivan.jdrop.user.domain.security;

import io.vavr.control.Option;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import top.ivan.jdrop.exchange.userinterface.vo.security.SignVO;
import top.ivan.jdrop.tool.IDUtils;
import top.ivan.jdrop.tool.cache.LimitCache;
import top.ivan.jdrop.user.application.UserServiceProvider;
import top.ivan.jdrop.user.domain.model.AccessUserEntity;
import top.ivan.jdrop.user.domain.repo.UserRepository;

import javax.annotation.Resource;

/**
 * @author Ivan
 * @since 2024/03/27 17:02
 */
@Slf4j
@Service
public class ValidService {

    private final LimitCache<String> limitCache = new LimitCache<>();

    @Resource
    private UserServiceProvider userServiceProvider;

    @Resource
    private UserRepository userRepository;

    public String takeValidToken(String userId) {
        String token = IDUtils.get32UUID();
        limitCache.register(userId, () -> token, 1, 60 * 1000);
        return token;
    }

    public boolean valid(String id, String sign, String data) {
        LimitCache.CacheData<String> cd = limitCache.getCacheData(id);
        if (!cd.isAccessible()) {
            log.warn("not accessible(unauthorized/expired): id({})", id);
            return false;
        } else {
            String token = cd.getData();
            log.debug("valid info: id({}), sign({}), data({})", id, token, data);
            return findUser(id).map(user -> user.validContent(buildValidContent(token, data), sign))
                    .filter(Boolean.TRUE::equals)
                    .getOrElse(() -> {
                        log.warn("valid token failed: {}", token);
                        return false;
                    });
        }
    }

    public Mono<SignVO> sign(String userId, String data) {
        String token = IDUtils.get32UUID();
        String content = buildValidContent(token, data);
        return userServiceProvider.sign(userId, content)
                .map(sign -> new SignVO(sign, token));
    }

    private Option<AccessUserEntity> findUser(String id) {
        return Option.of(userRepository.findUser(id));
    }

    private static String buildValidContent(String token, String data) {
        return String.join(";", data, token);
    }
}
