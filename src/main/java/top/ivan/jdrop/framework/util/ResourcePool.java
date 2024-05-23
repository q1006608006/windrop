package top.ivan.jdrop.framework.util;

import java.util.function.Supplier;

public class ResourcePool<T> {

    private final Supplier<T> supplier;
    private final CircularQueue<T> queue;

    public ResourcePool(Supplier<T> supplier, int maxSize) {
        this.supplier = supplier;
        queue = new CircularQueue(maxSize);
    }

    public T take() {
        T result = queue.dequeue();
        return supplier.get();
    }

    public boolean record(T resource) {
        return queue.enqueue(resource);
    }
}
