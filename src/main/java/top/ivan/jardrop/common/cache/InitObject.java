package top.ivan.jardrop.common.cache;

import java.util.function.Supplier;

/**
 * @author Ivan
 * @since 2024/03/12 15:11
 */
public class InitObject<T> {
    private final Supplier<T> supplier;
    private volatile boolean isInit;

    private T obj;

    public InitObject(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public boolean isInit() {
        return isInit;
    }

    public T get() {
        if (!isInit) {
            synchronized (this) {
                if (!isInit) {
                    obj = supplier.get();
                    isInit = true;
                }
            }
        }
        return obj;
    }
}
