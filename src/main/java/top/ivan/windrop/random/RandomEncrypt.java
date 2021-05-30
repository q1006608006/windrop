package top.ivan.windrop.random;

import top.ivan.windrop.ex.BadEncryptException;
import top.ivan.windrop.util.ConvertUtil;
import top.ivan.windrop.util.SystemUtil;

import java.util.Objects;

/**
 * @author Ivan
 * @since 2021/05/28 17:36
 */
public class RandomEncrypt {
    private final RandomAccessKey key;

    public RandomEncrypt(int interval) {
        key = new RandomAccessKey(interval);
    }

    public String encrypt(String data) throws BadEncryptException {
        return ConvertUtil.encrypt(data, key.getAccessKey());
    }

    public String decrypt(String content, boolean expired) throws BadEncryptException {
        String ck = key.getAccessKey();
        if (key.match(k -> Objects.equals(ck, k), expired)) {
            return ConvertUtil.decrypt(content, ck);
        }
        throw new BadEncryptException("数据已过期");
    }

    public void update() {
        key.tryUpdate();
    }
}
