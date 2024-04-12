package top.ivan.jdrop.exchange.security.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import top.ivan.jdrop.common.cache.CacheNotAccessableException;
import top.ivan.jdrop.common.cache.LimitCache;
import top.ivan.jdrop.common.exception.IllegalClientRequestException;
import top.ivan.jdrop.common.util.IDUtils;
import top.ivan.jdrop.common.util.WrapperUtils;
import top.ivan.jdrop.exchange.security.userinterface.vo.SignVO;
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

    public String takeValidToken(String userId) {
        String token = IDUtils.get32UUID();
        limitCache.register(userId, () -> token, 1, 60 * 1000);
        return token;
    }

    public boolean valid(String userId, String sign, String data) {
        LimitCache.CacheData<String> cd = limitCache.getCacheData(userId);
        if (cd.isAccessible()) {
            String token = cd.getData();
            log.debug("valid info: userId({}), sign({}), data({})", userId, token, data);
            boolean pass = WrapperUtils.isTrue(userActionService.validSign(userId, sign, buildValidContent(token, data)).block());
            if (pass) {
                return true;
            } else {
                log.warn("valid token failed: {}", token);
            }
        }
        return false;
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
