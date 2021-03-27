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
import java.util.function.Supplier;

/**
 * @author Ivan
 * @description
 * @date 2021/3/23
 */
@Slf4j
@Service
public class ResourceSharedService {

    private final Map<String, Object> resourceMap;

    @Autowired
    private ScheduledService scheduled;

    public ResourceSharedService() {
        resourceMap = new ConcurrentHashMap<>();
    }

    public void register(String key, File file) {
        resourceMap.put(key, ((Supplier<Resource>) () -> new FileSystemResource(file)));
        scheduleRemove(key);
    }

    public void register(String key, byte[] buff) {
        resourceMap.put(key, ((Supplier<Resource>) () -> new ByteArrayResource(buff)));
        scheduleRemove(key);
    }

    public void scheduleRemove(String key) {
        scheduled.schedule(() -> {
            log.debug("remove cached resource with key '{}'", key);
            resourceMap.remove(key);
        }, 5, TimeUnit.MINUTES);
    }

    public Resource findResource(String key) {
        Object target = resourceMap.get(key);
        if (null == target) {
            return null;
        }
        Resource resource;
        if (target instanceof Supplier) {
            resource = ((Supplier<Resource>) target).get();
            resourceMap.put(key, resource);
        } else {
            resource = (Resource) target;
        }
        return resource;
    }

    public void remove(String key) {
        resourceMap.remove(key);
    }
}
