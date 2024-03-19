package top.ivan.jardrop.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import top.ivan.jardrop.common.cache.LimitCache;
import top.ivan.jardrop.common.schedule.ScheduledService;

/**
 * @author Ivan
 * @since 2024/01/10 17:02
 */
@Slf4j
@Configuration
@EnableScheduling
public class CommonConfiguration {

    @Bean
    public ScheduledService getScheduledService() {
        return new ScheduledService(3);
    }

    @Bean
    public LimitCache getLimitCache() {
        return new LimitCache();
    }
}
