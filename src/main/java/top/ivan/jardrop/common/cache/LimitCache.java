package top.ivan.jardrop.common.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import top.ivan.jardrop.common.challenge.AccessLimiter;
import top.ivan.jardrop.common.challenge.ChallengeTask;
import top.ivan.jardrop.common.util.IDUtils;
import top.ivan.jardrop.qrcode.CacheNotAccessableException;

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

    private final ConcurrentHashMap<String, InitObject<T>> supplierMap = new ConcurrentHashMap<>();

    private final AccessLimiter accessLimiter = new AccessLimiter();

    public String register(Function<String, T> supplier, int count, int second) {
        String key = IDUtils.getShortUuid();
        register(key, () -> supplier.apply(key), count, second);
        return key;
    }

    public void register(String key, Supplier<T> supplier, int count, int second) {
        if (count < 1) {
            count = Integer.MAX_VALUE;
        }
        if (second < 1) {
            second = 60;
        }
        accessLimiter.register(key, second * 1000L, count, task -> supplierMap.remove(key, supplier));
        supplierMap.put(key, new InitObject<>(supplier));
    }

    public T getData(String key) throws CacheNotAccessableException {
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

    public CacheData<T> getCacheData(String key) {
        InitObject<T> supplier = supplierMap.get(key);
        ChallengeTask.State state = accessLimiter.challenge(key);
        return new CacheData<>(state, supplier);
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
            return supplier.get();
        }
    }


}
