package top.ivan.windrop.svc;


import org.springframework.stereotype.Service;
import top.ivan.windrop.WinDropConfiguration;
import top.ivan.windrop.util.RandomAccessKey;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * @author Ivan
 * @description
 * @date 2021/3/30
 */
@Service
public class RandomAccessKeyService {

    private final Map<String, RandomAccessKey> groupMap = new ConcurrentHashMap<>();

    public String getKey(String group) {
        return groupMap.computeIfAbsent(group, g -> new RandomAccessKey(30)).getAccessKey();
    }

    public boolean match(String group, Predicate<String> test) {
        RandomAccessKey key = groupMap.get(group);
        return key != null && key.match(test, true);
    }

}
