package top.ivan.windrop.util;

import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.Objects;

/**
 * @author Ivan
 * @description
 * @date 2021/4/20
 */
public class SpringUtil {
    public static String getIP(ServerHttpRequest request) {
        return Objects.requireNonNull(request.getRemoteAddress()).getAddress().getHostAddress();
    }
}
