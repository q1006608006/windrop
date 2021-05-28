package top.ivan.windrop.random;

import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

public class ChallengeTask<T> extends ReentrantLock {

    public enum State {
        SUCCESS, FAILED, NONE, TIMEOUT
    }

    private final long expireMillions;
    private final Predicate<T> keyAction;
    private Runnable onClean;
    private final LongAdder accessCounter = new LongAdder();
    private final LongAdder successCounter = new LongAdder();

    public ChallengeTask(long intervalMillions, Predicate<T> keyAction) {
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

    State challenge(T data) {
        if (isTimeout()) {
            return State.TIMEOUT;
        }
        accessCounter.increment();
        if (keyAction.test(data)) {
            successCounter.increment();
            return State.SUCCESS;
        }
        return State.FAILED;
    }

    void onClean(Runnable run) {
        this.onClean = run;
    }

    void clean() {
        if (null != onClean) onClean.run();
    }

    public long getSuccess() {
        return successCounter.longValue();
    }

    public long getAccess() {
        return accessCounter.longValue();
    }
}