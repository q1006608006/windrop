package top.ivan.windrop.verify;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import top.ivan.windrop.exception.HttpClientException;
import top.ivan.windrop.exception.HttpServerException;
import top.ivan.windrop.exception.UnCatchableException;

import java.util.function.Supplier;

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

    @Around("verifyPoint()")
    public Object verifyIP(ProceedingJoinPoint pjp) {
        MethodSignature ms = (MethodSignature) pjp.getSignature();
        Class<?> rt = ms.getReturnType();
        Supplier<?> supplier = () -> {
            try {
                return pjp.proceed();
            } catch (HttpClientException | HttpServerException e) {
                throw e;
            } catch (Throwable throwable) {
                throw new UnCatchableException(throwable);
            }
        };
        if (Mono.class.isAssignableFrom(rt)) {
            return verify().flatMap(v -> (Mono<?>) supplier.get());
        } else if (Flux.class.isAssignableFrom(rt)) {
            return verify().flatMapMany(s -> (Flux<?>) supplier.get());
        } else {
            verifyIP(WebHandler.getLocalExchange().getRequest().getRemoteAddress().getAddress().getHostAddress());
            return supplier.get();
        }
    }

    private Mono<String> verify() {
        return WebHandler.ip().doOnNext(this::verifyIP);
    }

    private void verifyIP(String ip) {
        if (!ipVerifier.accessible(ip)) {
            log.warn("unavailable ip: {}", ip);
            throw new HttpClientException(HttpStatus.FORBIDDEN, "未授予白名单");
        }
    }
}
