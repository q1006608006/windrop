package top.ivan.jardrop.user.domain;

import lombok.Data;

/**
 * @author Ivan
 * @description
 * @date 2021/3/11
 */
@Data
public class AccessUserEntity {
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


    /**
     * 验证内容
     *
     * @param content   原文
     * @param signature 签名
     * @return 验证结果，ture: 通过，false: 未通过
     */
    public boolean validContent(String content, String signature) {
        //todo
        return false;
    }

    public String sign(String content) {
        //todo
        return null;
    }
}
