package top.ivan.windrop.svc;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.ivan.windrop.ex.CacheNotAccessException;
import top.ivan.windrop.ex.CacheNotFoundException;
import top.ivan.windrop.ex.CacheTimeoutException;
import top.ivan.windrop.util.ChallengeKeys;
import top.ivan.windrop.util.IDUtil;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Ivan
 * @description
 * @date 2021/3/26
 */
@Service
public class QrCodeControllerService {

    private final ConcurrentHashMap<String, Supplier<String>> dataSupplierMap = new ConcurrentHashMap<>();

    private final ChallengeKeys challengeKeys = new ChallengeKeys(256);

    @Autowired
    private ResourceSharedService sharedService;

    @Autowired
    private ScheduledService scheduledService;

    public String register(Function<String, String> supplier, int count, int second) {
        String key = IDUtil.getShortUuid();
        register(key, () -> supplier.apply(key), count, second);
        return key;
    }

    public String sharedFile(File file, int count, int second) {
        String sharedKey = IDUtil.getShortUuid();
        sharedService.register(sharedKey, file, count, second);
        JSONObject obj = new JSONObject();
        obj.put("support", "file");
        obj.put("code", sharedKey);
        String qrCodeBody = obj.toJSONString();
        return register(key -> qrCodeBody, count, second);
    }

    public void register(String key, Supplier<String> supplier, int count, int second) {
        if (count < 1) {
            count = Integer.MAX_VALUE;
        }
        if (second < 1) {
            second = 60;
        }
        AtomicInteger counter = new AtomicInteger(count);
        ChallengeKeys.ChallengeTask task = challengeKeys.registerKey(key, second * 1000, t -> counter.decrementAndGet() > -1);
        dataSupplierMap.put(key, supplier);

        scheduledService.schedule(() -> {
            if (dataSupplierMap.remove(key, supplier)) {
                scheduledService.schedule(() -> challengeKeys.remove(key, task), 1, TimeUnit.MINUTES);
            }
        }, second, TimeUnit.SECONDS);
    }

    public String getData(String key) throws CacheNotFoundException, CacheNotAccessException, CacheTimeoutException {
        switch (challengeKeys.challenge(key)) {
            case NONE:
                throw new CacheNotFoundException("no resource for key '" + key + "'");
            case FAILED:
                throw new CacheNotAccessException("resource for key '" + key + "' access limited");
            case TIMEOUT:
                throw new CacheTimeoutException("resource for key '" + key + "' expired");
        }
        return dataSupplierMap.get(key).get();
    }
}
