package top.ivan.windrop.svc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * @author Ivan
 * @description
 * @date 2021/3/23
 */
@Slf4j
@Service
public class ResourceSharedService {

    private final Map<String, Supplier<Resource>> resourceMap;

    @Autowired
    private ScheduledService scheduled;

    public ResourceSharedService() {
        resourceMap = new ConcurrentHashMap<>();
    }

    public void register(String key, File file, int sharedCount, int cachedSecond) {
        register(key, () -> new FileSystemResource(file), sharedCount, cachedSecond);
    }

    public void register(String key, byte[] buff, int sharedCount, int cachedSecond) {
        register(key, () -> new ByteArrayResource(buff), sharedCount, cachedSecond);
    }

    public void register(String key, Resource resource, int sharedCount, int cachedSecond) {
        register(key, () -> resource, sharedCount, cachedSecond);
    }

    public void register(String key, Supplier<Resource> supplier, int sharedCount, int cachedSecond) {
        if (sharedCount < 1) {
            sharedCount = Integer.MAX_VALUE;
        }
        AtomicInteger counter = new AtomicInteger(sharedCount);
        resourceMap.put(key, () -> counter.getAndDecrement() > 0 ? supplier.get() : null);
        scheduleRemove(key, cachedSecond);
    }

    public void scheduleRemove(String key, int second) {
        if (second < 0) {
            second = 60;
        }
        scheduled.schedule(() -> {
            log.debug("remove cached resource with key '{}'", key);
            resourceMap.remove(key);
        }, second, TimeUnit.SECONDS);
    }

    public Resource findResource(String key) {
        Supplier<Resource> target = resourceMap.get(key);
        if (null == target) {
            return null;
        }
        return target.get();
    }

    public void remove(String key) {
        resourceMap.remove(key);
    }
}
