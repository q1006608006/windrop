package top.ivan.windrop.verify;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import top.ivan.windrop.ex.HttpServerException;

import java.util.Objects;

/**
 * @author Ivan
 * @since 2021/05/28 15:50
 */
public class WebHandler {

    public static ThreadLocal<ServerWebExchange> local = new ThreadLocal<>();

    public static void setExchange(ServerWebExchange ex) {
        local.set(ex);
    }

    public static ServerWebExchange getLocalExchange() {
        ServerWebExchange ex = local.get();
        if (null == ex) {
            throw new HttpServerException("can't supply ServerWebExchange in current");
        }
        return ex;
    }

    public static Mono<String> ip() {
        return request().map(req -> Objects.requireNonNull(req.getRemoteAddress()).getAddress().getHostAddress());
    }

    public static Mono<ServerHttpRequest> request() {
        return exchange().map(ServerWebExchange::getRequest);
    }

    public static Mono<ServerWebExchange> exchange() {
        return Mono.subscriberContext().map(ctx -> ctx.get(ServerWebExchange.class));
    }

    public static Mono<WebSession> session() {
        return exchange().flatMap(ServerWebExchange::getSession);
    }
}
