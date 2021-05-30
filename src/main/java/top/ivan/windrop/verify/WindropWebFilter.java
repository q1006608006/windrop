package top.ivan.windrop.verify;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * @author Ivan
 * @since 2021/05/27 17:33
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@Slf4j
@Order(-1)
public class WindropWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        WebHandler.setExchange(exchange);
        log.debug("receive request from '{}', path: {}, method: {}", WebHandler.getRemoteAddress(), WebHandler.getRequest().getPath(), WebHandler.getRequest().getMethod());
        return chain.filter(exchange).doFinally(s -> WebHandler.release());
    }

}
