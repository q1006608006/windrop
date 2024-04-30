package top.ivan.jdrop.framework.boot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ScheduledExecutorFactoryBean;

import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Ivan
 * @since 2024/01/10 17:02
 */
@Slf4j
@Configuration
@EnableScheduling
public class CommonConfiguration {
    @Bean
    public ScheduledExecutorService getScheduledService() {
        ScheduledExecutorFactoryBean factoryBean = new ScheduledExecutorFactoryBean();
        factoryBean.setThreadGroupName("windrop.scheduled");
        factoryBean.setPoolSize(3);
        factoryBean.setThreadNamePrefix("scheduled-task-");
        factoryBean.initialize();
        return factoryBean.getObject();
    }

}
