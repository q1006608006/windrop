package top.ivan.windrop.util;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.*;

/**
 * @author Ivan
 * @description
 * @date 2021/3/25
 */
@Slf4j
public class QueuedConcurrentMap<K, V> extends ConcurrentHashMap<K, V> {

    private final int maxSize;
    private final transient LinkedBlockingQueue<K> keyQueue;
    private final transient BiPredicate<K, V> removeEldest;
    private final transient BiConsumer<K, V> cleanAction;

    private final ReentrantLock releaseLock = new ReentrantLock();

    public QueuedConcurrentMap(int max, BiPredicate<K, V> removeEldest, BiConsumer<K, V> cleanAction) {
        this.maxSize = max;
        this.keyQueue = new LinkedBlockingQueue<>();
        this.removeEldest = removeEldest;
        this.cleanAction = cleanAction == null ? (k, v) -> {
        } : cleanAction;
    }

    public QueuedConcurrentMap(int max, BiPredicate<K, V> removeEldest) {
        this(max, removeEldest, null);
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

    private V doThenCheckFull(K key, Supplier<V> supplier) {
        if (!containsKey(key)) {
            keyQueue.add(key);
        }
        V r = supplier.get();
        int keySize = keyQueue.size();
        if (keySize > maxSize) {
            checkForFull();
        }
        return r;
    }

    private void checkForFull() {
        if (null == removeEldest) {
            return;
        }
        if (keyQueue.size() > maxSize) {
            Iterator<K> itr = keyQueue.iterator();
            while (itr.hasNext()) {
                K key = itr.next();
                V val = get(key);
                if (removeEldest.test(key, val) && remove(key, val)) {
                    itr.remove();
                    cleanAction.accept(key, val);
                    if (keyQueue.size() <= maxSize) {
                        return;
                    }
                }
            }
        }

        // force remove
        if (keyQueue.size() > maxSize) {
            try {
                if (releaseLock.tryLock()) {
                    int eleSize = keyQueue.size() - maxSize;
                    ArrayList<K> removeKeys = new ArrayList<>(eleSize);
                    keyQueue.drainTo(removeKeys, eleSize);
                    for (K key : removeKeys) {
                        V val = remove(key);
                        cleanAction.accept(key, val);
                        log.warn("size is over limit, force remove {'{}': '{}'}", key, val);
                    }
                }
            } finally {
                if (releaseLock.isHeldByCurrentThread()) {
                    releaseLock.unlock();
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
