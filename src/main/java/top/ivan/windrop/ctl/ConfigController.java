package top.ivan.windrop.ctl;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

/**
 * @author Ivan
 * @since 2023/06/29 15:52
 */
@RequestMapping("config")
public class ConfigController {

    @GetMapping
    public Mono<String> getConfig() {
        return Mono.empty();
    }

}
