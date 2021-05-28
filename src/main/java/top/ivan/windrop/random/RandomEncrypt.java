package top.ivan.windrop.random;

import top.ivan.windrop.ex.BadEncryptException;
import top.ivan.windrop.util.ConvertUtil;

/**
 * @author Ivan
 * @since 2021/05/28 17:36
 */
public class RandomEncrypt {
    private final RandomAccessKey key;

    public RandomEncrypt(int interval) {
        key = new RandomAccessKey(interval);
    }

    public String encrypt(String data) {
        return ConvertUtil.encrypt(data, key.getAccessKey());
    }

    public String decrypt(String content) throws BadEncryptException {
        String ck = key.getAccessKey();
        return ConvertUtil.decrypt(content, key.getAccessKey());
    }
}
