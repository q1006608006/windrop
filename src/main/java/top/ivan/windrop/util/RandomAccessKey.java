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

    private void untilUpdated() {
        if (!tryUpdate()) waitUpdate();
    }

    private void waitUpdate() {
        Object oldKey = accessKey;
        if (isUpdate.get()) {
            while (oldKey == accessKey) ;
        }
    }

    public String getAccessKey() {
        if (isTimeout()) {
            untilUpdated();
        } else {
            waitUpdate();
        }
        return accessKey;
    }

    public boolean match(Predicate<String> predicate, boolean matchThenUpdate) {
        if (isTimeout()) {
            tryUpdate();
            return false;
        }

        Object oldKey = accessKey;
        while (true) {
            if (inCompare.compareAndSet(false, true)) {
                if (oldKey != accessKey) {
                    return false;
                }
                if (isTimeout()) {
                    tryUpdate();
                    return false;
                }
                long curLastUpdateTime = lastUpdateTime;
                try {
                    boolean status = false;
                    if (predicate.test(accessKey)) {
                        if (matchThenUpdate) {
//                            untilUpdated();
                            if (isUpdate.compareAndSet(false, true)) {
                                if (curLastUpdateTime == lastUpdateTime) {
                                    lastUpdateTime = curLastUpdateTime - intervalMillis - 1;
                                }
                                isUpdate.set(false);
                            }
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
