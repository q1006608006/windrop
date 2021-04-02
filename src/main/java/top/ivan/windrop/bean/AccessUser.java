package top.ivan.windrop.bean;

import lombok.Data;

/**
 * @author Ivan
 * @description
 * @date 2021/3/11
 */
@Data
public class AccessUser {
    private String id;
    private String validKey;
    private String alias;
    private long accessTime;
    private long expireTime;

    public boolean isExpired() {
        return isExpired(System.currentTimeMillis());
    }

    public boolean isExpired(long millis) {
        if (expireTime < 0) {
            return false;
        }
        return millis > expireTime;
    }
}
