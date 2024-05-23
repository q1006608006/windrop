package top.ivan.jdrop.framework.util.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import top.ivan.jdrop.framework.util.IDUtils;
import top.ivan.jdrop.framework.util.challenge.AccessLimiter;
import top.ivan.jdrop.framework.util.challenge.ChallengeTask;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Ivan
 * @description
 * @date 2021/3/26
 */
@Slf4j
public class LimitCache<T> {

    private static final CacheData<?> NONE = new CacheData<>(ChallengeTask.State.NONE, null);
    private final ConcurrentHashMap<String, InitObject<T>> supplierMap = new ConcurrentHashMap<>();

    private final AccessLimiter accessLimiter;

    public LimitCache(int maxSize) {
        this.accessLimiter = new AccessLimiter(maxSize);
    }

    public String register(Function<String, T> supplier, int maxAccessCount, int expireMillions) {
        String key = IDUtils.getShortUuid();
        register(key, () -> supplier.apply(key), maxAccessCount, expireMillions);
        return key;
    }

    public void register(@NonNull String key, Supplier<T> supplier, int maxAccessCount, int expireMillions) {
        if (maxAccessCount < 1) {
            maxAccessCount = Integer.MAX_VALUE;
        }
        if (expireMillions < 1) {
            expireMillions = Integer.MAX_VALUE;
        }

        InitObject<T> obj = new InitObject<>(supplier);
        supplierMap.put(key, obj);
        accessLimiter.register(key, expireMillions, maxAccessCount, task -> supplierMap.remove(key, obj));
    }

    public T getData(@NonNull String key) throws CacheNotAccessableException {
        InitObject<T> supplier = supplierMap.get(key);

        switch (accessLimiter.challenge(key)) {
            case NONE:
                throw new CacheNotAccessableException("no resource for key '" + key + "'");
            case FAILED:
                throw new CacheNotAccessableException("resource for key '" + key + "' access limited");
            case TIMEOUT:
                throw new CacheNotAccessableException("resource for key '" + key + "' expired");
        }

        if (null == supplier) {
            throw new CacheNotAccessableException("resource for key '" + key + "' expired");
        }

        return supplier.get();
    }

    @SuppressWarnings("unchecked")
    public CacheData<T> getCacheData(@NonNull String key) {
        if (!supplierMap.containsKey(key)) {
            return (CacheData<T>) NONE;
        }
        InitObject<T> supplier = supplierMap.get(key);
        ChallengeTask.State state = accessLimiter.challenge(key);
        return new CacheData<>(state, supplier);
    }

    public boolean exist(@NonNull String key) {
        return supplierMap.containsKey(key);
    }

    public void remove(@NonNull String key) {
        supplierMap.remove(key);
        accessLimiter.remove(key);
    }

    @AllArgsConstructor
    public static class CacheData<V> {
        @Getter
        private ChallengeTask.State state;
        private InitObject<V> supplier;

        public boolean isTimeout() {
            return state == ChallengeTask.State.TIMEOUT;
        }

        public boolean isLimit() {
            return state == ChallengeTask.State.FAILED;
        }

        public boolean isExist() {
            return state != ChallengeTask.State.NONE;
        }

        public boolean isAccessible() {
            return state == ChallengeTask.State.SUCCESS;
        }

        public V getData() {
            if (!isAccessible()) {
                return null;
            }
            return peek();
        }

        public V peek() {
            return supplier == null ? null : supplier.get();
        }
    }

}
