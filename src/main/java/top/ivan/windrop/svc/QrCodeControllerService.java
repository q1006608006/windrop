package top.ivan.windrop.svc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.ivan.windrop.exception.CacheNotAccessException;
import top.ivan.windrop.exception.CacheNotFoundException;
import top.ivan.windrop.exception.CacheTimeoutException;
import top.ivan.windrop.random.ChallengeTask;
import top.ivan.windrop.random.CounterAccessKeyService;
import top.ivan.windrop.util.IDUtil;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Ivan
 * @description
 * @date 2021/3/26
 */
@Slf4j
@Service
public class QrCodeControllerService {

    private final ConcurrentHashMap<String, Supplier<String>> dataSupplierMap = new ConcurrentHashMap<>();

    @Autowired
    private CounterAccessKeyService accessService;

    public String register(Function<String, String> supplier, int count, int second) {
        String key = IDUtil.getShortUuid();
        register(key, () -> supplier.apply(key), count, second);
        return key;
    }

    public void register(String key, Supplier<String> supplier, int count, int second) {
        if (count < 1) {
            count = Integer.MAX_VALUE;
        }
        if (second < 1) {
            second = 60;
        }
        ChallengeTask<Void> task = accessService.register(key, second * 1000L, 3 * 60 * 1000L, count);
        dataSupplierMap.put(key, supplier);
        task.onClean(() -> dataSupplierMap.remove(key, supplier));
    }

    public String getData(String key) throws CacheNotFoundException, CacheNotAccessException, CacheTimeoutException {
        switch (accessService.challenge(key)) {
            case NONE:
                throw new CacheNotFoundException("no resource for key '" + key + "'");
            case FAILED:
                throw new CacheNotAccessException("resource for key '" + key + "' access limited");
            case TIMEOUT:
                throw new CacheTimeoutException("resource for key '" + key + "' expired");
        }
        Supplier<String> supplier = dataSupplierMap.get(key);
        if (null == supplier) {
            throw new CacheTimeoutException("resource for key '" + key + "' expired");
        }
        return supplier.get();
    }

}
