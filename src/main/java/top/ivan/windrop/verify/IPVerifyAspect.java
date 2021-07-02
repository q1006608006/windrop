package top.ivan.windrop.verify;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import top.ivan.windrop.ex.HttpClientException;

/**
 * @author Ivan
 * @since 2021/05/27 17:19
 */
@Aspect
@Component
@Order(1)
@Slf4j
public class IPVerifyAspect {

    @Autowired
    private IPVerifier ipVerifier;

    @Pointcut("@annotation(VerifyIP)")
    public void verifyPoint() {
    }

    @Before("verifyPoint()")
    public Mono<Void> verifyIP() {
        return verifyIp();
    }

    private Mono<Void> verifyIp() {
        String ip = WebHandler.getRemoteIP();
        if (!ipVerifier.accessible(ip)) {
            log.warn("unavailable ip from: {}", WebHandler.getRemoteAddress());
            throw new HttpClientException(HttpStatus.FORBIDDEN, "未授予白名单");
        }
    }
}
