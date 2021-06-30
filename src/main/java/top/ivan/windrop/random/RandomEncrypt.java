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

    public String encrypt(String data) throws BadEncryptException {
        return ConvertUtil.encrypt(data, key.getAccessKey());
    }

    public String decrypt(String content) throws BadEncryptException {
        if (!key.isTimeout()) {
            String decryptKey = key.getOriginKey();
            try {
                return ConvertUtil.decrypt(content, decryptKey);
            } catch (Exception e) {
                throw new BadEncryptException("数据状态异常", e);
            }
        }
        throw new BadEncryptException("数据已过期");
    }

    public void update() {
        key.tryUpdate();
    }

    public void expired() {
        key.expired();
    }
}
