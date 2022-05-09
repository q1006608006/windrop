package top.ivan.windrop.random;

import top.ivan.windrop.util.IDUtil;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public class RandomAccessKey {

    private final long intervalMillis;
    private volatile String accessKey;
    private volatile long lastUpdateTime;

    private final AtomicBoolean inUpdate = new AtomicBoolean(false);
    private final AtomicBoolean inChangeTime = new AtomicBoolean(false);

    public RandomAccessKey(int interval) {
        this.intervalMillis = interval * 1000L;
    }

    public boolean tryUpdate() {
        if (inUpdate.compareAndSet(false, true)) {
            accessKey = IDUtil.getShortUuid();

            while (!inChangeTime.compareAndSet(false, true)) {
            }
            lastUpdateTime = getTime();
            inChangeTime.set(false);
            inUpdate.set(false);
            return true;
        }
        return false;
    }

    private void waitUpdate() {
        while (inUpdate.get()) ;
    }

    public String getAccessKey() {
        if (isTimeout() && !tryUpdate()) {
            waitUpdate();
        }
        return accessKey;
    }

    public boolean match(Predicate<String> predicate, boolean matchThenExpired) {
        long curLastUpdateTime = lastUpdateTime;

        if (isTimeout()) {
            tryUpdate();
            return false;
        }

        if (!inUpdate.get() && predicate.test(accessKey)) {
            if (matchThenExpired) {
                return expired(curLastUpdateTime);
            } else {
                return true;
            }
        }

        return false;
    }

    public String getOriginKey() {
        return accessKey;
    }

    public boolean expired() {
        return expired(lastUpdateTime);
    }

    public boolean expired(long compare) {
        if(inChangeTime.compareAndSet(false,true)) {
            try {
                if (compare == lastUpdateTime) {
                    lastUpdateTime = -1;
                    return true;
                }
            } finally {
                inChangeTime.set(false);
            }
        }
        return false;
    }

    public boolean isTimeout() {
        return isTimeout(getTime());
    }

    public boolean isTimeout(long time) {
        return time > lastUpdateTime + intervalMillis;
    }

    private long getTime() {
        return System.currentTimeMillis();
    }

    public int getInterval() {
        return (int) intervalMillis / 1000;
    }

}
