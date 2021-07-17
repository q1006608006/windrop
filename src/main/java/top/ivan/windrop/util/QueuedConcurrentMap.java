package top.ivan.windrop.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.*;

/**
 * @author Ivan
 * @description
 * @date 2021/3/25
 */
public class QueuedConcurrentMap<K, V> extends ConcurrentHashMap<K, V> {

    private final int maxSize;
    private final transient LinkedBlockingQueue<K> keyQueue;
    private final transient BiPredicate<K, V> removeEldest;
    private final transient BiConsumer<K, V> cleanAction;

    public QueuedConcurrentMap(int max, BiPredicate<K, V> removeEldest, BiConsumer<K, V> cleanAction) {
        this.maxSize = max;
        this.keyQueue = new LinkedBlockingQueue<>(maxSize * 2);
        this.removeEldest = removeEldest == null ? (k, v) -> true : removeEldest;
        this.cleanAction = cleanAction == null ? (k, v) -> {
        } : cleanAction;
    }

    public QueuedConcurrentMap(int max, BiConsumer<K, V> cleanAction) {
        this(max, null, cleanAction);
    }

    public QueuedConcurrentMap(int max) {
        this(max, null, null);
    }

    @Override
    public V put(K key, V value) {
        return doThenCheckFull(key, () -> super.put(key, value));
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return doThenCheckFull(key, () -> super.putIfAbsent(key, value));
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return doThenCheckFull(key, () -> super.computeIfAbsent(key, mappingFunction));
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return doThenCheckFull(key, () -> super.compute(key, remappingFunction));
    }

    public V doThenCheckFull(K key, Supplier<V> supplier) {
        if (!containsKey(key)) {
            keyQueue.add(key);
        }
        V r = supplier.get();
        if (size() > maxSize) {
            checkForFull();
            // force remove
            while (size() > maxSize && !keyQueue.isEmpty()) {
                K removedKey = keyQueue.poll();
                V removed = remove(removedKey);
                cleanAction.accept(removedKey, removed);
            }
        }
        return r;
    }

    private void checkForFull() {
        if (size() > maxSize) {
            List<K> list = new ArrayList<>(keyQueue);
            for (K k : list) {
                V v = get(k);
                if (removeEldest.test(k, v) && remove(k, v)) {
                    keyQueue.remove(k);
                    cleanAction.accept(k, v);
                    if (size() <= maxSize) {
                        return;
                    }
                }
            }
        }
    }

    @Override
    public V remove(Object key) {
        keyQueue.remove(key);
        return super.remove(key);
    }

    @Override
    public boolean remove(Object key, Object value) {
        if (super.remove(key, value)) {
            keyQueue.remove(key);
            return true;
        }
        return false;
    }
}
