package top.ivan.windrop.svc;


import org.springframework.stereotype.Service;
import top.ivan.windrop.util.QueuedConcurrentMap;
import top.ivan.windrop.random.RandomAccessKey;

import java.util.Map;
import java.util.function.Predicate;

/**
 * @author Ivan
 * @description
 * @date 2021/3/30
 */
@Service
public class RandomAccessKeyService {

    private final Map<String, RandomAccessKey> groupMap = new QueuedConcurrentMap<>(256);

    public String getKey(String group) {
        return groupMap.computeIfAbsent(group, g -> new RandomAccessKey(30)).getAccessKey();
    }

    public String getKey(String group, int interval) {
        return groupMap.computeIfAbsent(group, g -> new RandomAccessKey(interval)).getAccessKey();
    }

    public boolean match(String group, Predicate<String> test) {
        RandomAccessKey key = groupMap.get(group);
        return key != null && key.match(test, true);
    }

}
