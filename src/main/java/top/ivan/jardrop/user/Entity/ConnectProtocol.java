package top.ivan.jardrop.user.Entity;

import lombok.Data;
import org.apache.commons.codec.digest.DigestUtils;
import top.ivan.jardrop.common.util.IDUtils;

/**
 * @author Ivan
 * @since 2024/03/12 11:02
 */
@Data
public class ConnectProtocol {
    private final long expireMillions;
    private final String keyRoot;
    private final String tokenElement;
    private final String keyElement;

    public ConnectProtocol(int expireMillions) {
        this.expireMillions = expireMillions;
        keyRoot = IDUtils.get32UUID();
        tokenElement = IDUtils.get32UUID();
        keyElement = IDUtils.get32UUID();
    }

    public String toToken() {
        return DigestUtils.sha256Hex(keyRoot + tokenElement);
    }

    public String toValidKey() {
        return DigestUtils.sha256Hex(keyRoot + keyElement);
    }

}
