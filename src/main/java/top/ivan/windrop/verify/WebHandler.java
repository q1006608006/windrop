package top.ivan.windrop.verify;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

/**
 * @author Ivan
 * @since 2021/05/28 15:50
 */
public class WebHandler {

    public static ThreadLocal<ServerWebExchange> local = new ThreadLocal<>();

    public static void setExchange(ServerWebExchange ex) {
        local.set(ex);
    }

    public static ServerWebExchange getExchange() {
        ServerWebExchange ex = local.get();
        if (null == ex) {
            throw new RuntimeException("can't supply ServerWebExchange in current");
        }
        return ex;
    }

    public static void release() {
        local.remove();
    }

    public static String getRemoteIP() {
        return getRemoteAddress().getAddress().getHostAddress();
    }

    public static ServerHttpRequest getRequest() {
        return getExchange().getRequest();
    }

    public static InetSocketAddress getRemoteAddress() {
        return getRequest().getRemoteAddress();
    }

    public static Mono<String> ip() {

    }
}
