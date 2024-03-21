package top.ivan.jardrop.common.challenge;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @author Ivan
 * @since 2021/05/10 15:35
 */
public class AccessLimiter {
    private static final int MAX_SIZE = 256;
    private final ChallengeTaskManager<Void> challengeKeys;


    public AccessLimiter() {
        challengeKeys = new ChallengeTaskManager<>(MAX_SIZE);
    }

    public AccessLimiter(int size) {
        this.challengeKeys = new ChallengeTaskManager<>(size);
    }

    public void register(String key, long expireMillion, int maxAccessTimes, Consumer<ChallengeTask<Void>> onClean) {
        AtomicInteger counter = new AtomicInteger(maxAccessTimes);
        ChallengeTask<Void> task = challengeKeys.registerTask(key, expireMillion, t -> counter.decrementAndGet() > -1);
        task.onClean(() -> onClean.accept(task));
    }

    public ChallengeTask.State challenge(String key) {
        return challengeKeys.challenge(key, null);
    }

    public void remove(String key) {
        challengeKeys.remove(key);
    }

}
