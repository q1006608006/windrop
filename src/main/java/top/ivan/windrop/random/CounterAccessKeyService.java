package top.ivan.windrop.random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.ivan.windrop.svc.ScheduledService;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Ivan
 * @since 2021/05/10 15:35
 */
@Service
public class CounterAccessKeyService {
    private final ChallengeKeys<Void> challengeKeys = new ChallengeKeys<>(256);

    @Autowired
    private ScheduledService scheduledService;

    public ChallengeTask<Void> register(String key, long effect, long expired, int count) {
        AtomicInteger counter = new AtomicInteger(count);
        ChallengeTask<Void> task = challengeKeys.registerKey(key, effect, t -> counter.decrementAndGet() > -1);
        scheduledService.schedule(() -> challengeKeys.remove(key, task), effect + expired, TimeUnit.MILLISECONDS);
        return task;
    }

    public ChallengeTask.State challenge(String key) {
        return challengeKeys.challenge(key, null);
    }

    public void onClean(ChallengeTask<Void> task, Runnable run) {
        challengeKeys.onClean(task, run);
    }
}
