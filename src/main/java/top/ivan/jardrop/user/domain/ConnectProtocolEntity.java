package top.ivan.jardrop.user.domain;

import lombok.Data;
import org.apache.commons.codec.digest.DigestUtils;
import top.ivan.jardrop.common.util.IDUtils;

/**
 * @author Ivan
 * @since 2024/03/12 11:02
 */
@Data
public class ConnectProtocolEntity {
    private final long expireMillions;
    private final String keyRoot;
    private final String tokenElement;
    private final String keyElement;

    public ConnectProtocolEntity(int expireMillions) {
        this.expireMillions = expireMillions;
        keyRoot = IDUtils.get32UUID();
        tokenElement = IDUtils.get32UUID();
        keyElement = IDUtils.get32UUID();
    }

    public String toToken() {
        return ConnectProtocolEntity.toToken(keyRoot, tokenElement);
    }

    public String toValidKey() {
        return ConnectProtocolEntity.toValidKey(keyRoot, keyElement);
    }

    public static String toToken(String keyRoot, String tokenElement) {
        return DigestUtils.sha256Hex(keyRoot + tokenElement);
    }

    public static String toValidKey(String keyRoot, String keyElement) {
        return DigestUtils.sha256Hex(keyRoot + keyElement);
    }

}
