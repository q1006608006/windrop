package top.ivan.jdrop.security.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import top.ivan.jdrop.common.cache.CacheNotAccessableException;
import top.ivan.jdrop.common.cache.LimitCache;
import top.ivan.jdrop.common.exception.IllegalClientRequestException;
import top.ivan.jdrop.common.util.IDUtils;
import top.ivan.jdrop.security.userinterface.model.vo.SignVO;
import top.ivan.jdrop.user.application.UserActionService;

/**
 * @author Ivan
 * @since 2024/03/27 17:02
 */
@Slf4j
@Service
public class ValidService {

    private final LimitCache<String> limitCache = new LimitCache<>();

    @Autowired
    private UserActionService userActionService;

    public Mono<String> takeValidToken(String userId) {
        return Mono.just(IDUtils.get32UUID())
                .doOnNext(token -> limitCache.register(userId, () -> token, 1, 60 * 1000));
    }

    public Mono<Void> valid(String userId, String sign, String data) {
        return Mono.defer(() -> {
                    try {
                        return Mono.just(limitCache.getData(userId));
                    } catch (CacheNotAccessableException e) {
                        return Mono.error(e);
                    }
                })
                .doOnNext(token -> log.debug("valid info: userId({}), sign({}), data({})", userId, token, data))
                .flatMap(token -> userActionService.validSign(userId, sign, buildValidContent(token, data)))
                .doOnError(e -> log.warn("valid sign failed: {}", e.getMessage()))
                .flatMap(r -> Boolean.TRUE.equals(r) ? Mono.empty() : Mono.error(new IllegalClientRequestException("sign valid failed")));
    }

    public Mono<SignVO> sign(String userId, String data) {
        String token = IDUtils.get32UUID();
        String content = buildValidContent(token, data);
        return userActionService.sign(userId, content)
                .map(sign -> new SignVO(sign, token));
    }

    private static String buildValidContent(String token, String data) {
        return String.join(";", data, token);
    }
}
