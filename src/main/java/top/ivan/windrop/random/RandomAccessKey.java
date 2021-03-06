package top.ivan.windrop.random;

import top.ivan.windrop.util.IDUtil;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public class RandomAccessKey {

    private final long intervalMillis;
    private volatile String accessKey;
    private volatile long lastUpdateTime;

    private final AtomicBoolean isUpdate = new AtomicBoolean(false);
    private final AtomicBoolean inCompare = new AtomicBoolean(false);

    public RandomAccessKey(int interval) {
        this.intervalMillis = interval * 1000L;
    }

    public boolean tryUpdate() {
        if (isUpdate.compareAndSet(false, true)) {
            accessKey = IDUtil.getShortUuid();
            lastUpdateTime = getTime();
            isUpdate.set(false);
            return true;
        }
        return false;
    }

    private void syncUpdate() {
        if (!tryUpdate()) waitUpdate();
    }

    private void waitUpdate() {
        while (isUpdate.get()) ;
    }

    private void equalThenExpired(long compareTime) {
        if (compareTime == lastUpdateTime && isUpdate.compareAndSet(false, true)) {
            if (compareTime == lastUpdateTime) {
                lastUpdateTime = compareTime - intervalMillis - 1;
            }
            isUpdate.set(false);
        }
    }

    public String getAccessKey() {
        waitUpdate();
        if (isTimeout()) {
            syncUpdate();
        }
        return accessKey;
    }

    public boolean match(Predicate<String> predicate, boolean matchThenExpired) {
        if (isTimeout()) {
            tryUpdate();
            return false;
        }

        while (true) {
            if (inCompare.compareAndSet(false, true)) {
                long curLastUpdateTime = lastUpdateTime;
                if (isUpdate.get() || isTimeout()) {
                    return false;
                }
                try {
                    boolean status = false;
                    if (predicate.test(accessKey)) {
                        if (matchThenExpired) {
                            equalThenExpired(curLastUpdateTime);
                        }
                        status = true;
                    }
                    return status;
                } finally {
                    inCompare.set(false);
                }
            }
        }
    }

    public String getOriginKey() {
        return accessKey;
    }

    public void expired() {
        equalThenExpired(lastUpdateTime);
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
