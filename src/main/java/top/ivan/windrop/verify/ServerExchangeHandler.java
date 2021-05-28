package top.ivan.windrop.verify;

import org.springframework.web.server.ServerWebExchange;

/**
 * @author Ivan
 * @since 2021/05/28 15:50
 */
public class ServerExchangeHandler {

    public static ThreadLocal<ServerWebExchange> local = new ThreadLocal<>();

    public static void setExchange(ServerWebExchange ex) {
        local.set(ex);
    }

    public static ServerWebExchange getExchange() {
        return local.get();
    }

    public static void release() {
        local.remove();
    }
}
