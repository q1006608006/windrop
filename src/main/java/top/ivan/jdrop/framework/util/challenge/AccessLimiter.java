package top.ivan.jdrop.framework.util.challenge;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @author Ivan
 * @since 2021/05/10 15:35
 */
public class AccessLimiter {
    private final ChallengeTaskManager<Void> challengeKeys;

    public AccessLimiter(int size) {
        this.challengeKeys = new ChallengeTaskManager<>(size);
    }

    public void register(String key, long expireMillion, int maxAccessTimes, Consumer<ChallengeTask<Void>> onClean) {
        AtomicInteger counter = new AtomicInteger(maxAccessTimes);
        challengeKeys.registerTask(key, expireMillion, t -> counter.decrementAndGet() > -1, onClean);
    }

    public ChallengeTask.State challenge(String key) {
        return challengeKeys.challenge(key, null);
    }

    public void remove(String key) {
        challengeKeys.remove(key);
    }

}
