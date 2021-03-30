package top.ivan.windrop.svc;


import org.springframework.stereotype.Service;
import top.ivan.windrop.util.RandomAccessKey;

import java.util.function.Predicate;

/**
 * @author Ivan
 * @description
 * @date 2021/3/30
 */
@Service
public class ApplyControllerService {
    private final RandomAccessKey randomAccessKey = new RandomAccessKey(30);

    public boolean match(Predicate<String> test) {
        return randomAccessKey.match(test, true);
    }

    public String getKey() {
        return randomAccessKey.getAccessKey();
    }

}
