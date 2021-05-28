package top.ivan.windrop.verify;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import top.ivan.windrop.ex.HttpClientException;

import static top.ivan.windrop.util.SpringUtil.getIP;

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
    public void verifyIP() {
        ServerWebExchange exchange = ServerExchangeHandler.getExchange();
        verifyIp(exchange.getRequest());
    }

    private void verifyIp(ServerHttpRequest request) {
        String ip = getIP(request);
        if (!ipVerifier.accessible(ip)) {
            log.info("unavailable ip: {}", ip);
            throw new HttpClientException(HttpStatus.FORBIDDEN, "未授予白名单");
        }
    }
}
