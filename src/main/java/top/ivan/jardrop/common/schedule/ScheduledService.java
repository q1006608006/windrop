package top.ivan.jardrop.common.schedule;

import org.springframework.scheduling.concurrent.ScheduledExecutorFactoryBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Ivan
 * @description
 * @date 2021/3/26
 */
@Component
public class ScheduledService {
    private final ScheduledExecutorService scheduled;

    public ScheduledService(int core) {
        ScheduledExecutorFactoryBean factoryBean = new ScheduledExecutorFactoryBean();
        factoryBean.setThreadGroupName("windrop.scheduled");
        factoryBean.setPoolSize(core);
        factoryBean.setThreadNamePrefix("scheduled-task-");
        factoryBean.initialize();
        scheduled = factoryBean.getObject();
    }

    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return scheduled.schedule(command, delay, unit);
    }
}
