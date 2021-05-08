package top.ivan.windrop.util;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public class RandomAccessKey {

    private final int intervalMillis;
    private volatile String accessKey;
    private volatile long lastUpdateTime;

    private final AtomicBoolean isUpdate = new AtomicBoolean(false);
    private final AtomicBoolean inCompare = new AtomicBoolean(false);

    public RandomAccessKey(int interval) {
        this.intervalMillis = interval * 1000;
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

    private void setExpired(long compareTime) {
        if (isUpdate.compareAndSet(false, true)) {
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
                            setExpired(curLastUpdateTime);
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

    private boolean isTimeout() {
        return isTimeout(getTime());
    }

    public boolean isTimeout(long time) {
        return time > lastUpdateTime + intervalMillis;
    }

    private long getTime() {
        return System.currentTimeMillis();
    }

}
