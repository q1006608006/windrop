package top.ivan.windrop.util;

import org.springframework.lang.NonNull;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

/**
 * @author Ivan
 * @description
 * @date 2021/3/24
 */
public class ChallengeKeys {

    private final QueuedConcurrentMap<String, ChallengeTask> keyMap;

    public ChallengeKeys(int maxSize) {
        keyMap = new QueuedConcurrentMap<>(maxSize, (k, task) -> task.isTimeout());
    }

    public ChallengeTask registerKey(String key, long intervalMillions, @NonNull Predicate<ChallengeTask> test) {
        ChallengeTask task = new ChallengeTask(intervalMillions, test);
        keyMap.put(key, task);
        return task;
    }

    public ChallengeTask registerIfAbsent(String key, long intervalMillions, @NonNull Predicate<ChallengeTask> test) {
        ChallengeTask task = new ChallengeTask(intervalMillions, test);
        keyMap.computeIfAbsent(key, k -> new ChallengeTask(intervalMillions, test));
        return task;
    }

    public State challenge(String key) {
        ChallengeTask task = keyMap.get(key);
        if (null == task) {
            return State.NONE;
        }
        return task.challenge();
    }

    public ChallengeTask remove(String key) {
        return keyMap.remove(key);
    }

    public boolean remove(String key, ChallengeTask task) {
        return keyMap.remove(key, task);
    }

    public enum State {
        SUCCESS, FAILED, NONE, TIMEOUT
    }

    public static class ChallengeTask extends ReentrantLock {

        private final long expireMillions;
        private final Predicate<ChallengeTask> keyAction;

        public ChallengeTask(long intervalMillions, Predicate<ChallengeTask> keyAction) {
            if (intervalMillions > 0) {
                expireMillions = System.currentTimeMillis() + intervalMillions;
            } else {
                expireMillions = Long.MAX_VALUE;
            }
            this.keyAction = keyAction == null ? s -> true : keyAction;
        }

        public boolean isTimeout() {
            return System.currentTimeMillis() > expireMillions;
        }

        private State challenge() {
            if (isTimeout()) {
                return State.TIMEOUT;
            }
            return keyAction.test(this) ? State.SUCCESS : State.FAILED;
        }
    }

}
