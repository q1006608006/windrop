package top.ivan.jardrop.user.Entity;

import lombok.Data;
import org.apache.commons.codec.digest.DigestUtils;
import top.ivan.jardrop.common.util.IDUtils;

/**
 * @author Ivan
 * @since 2024/03/12 11:02
 */
@Data
public class Protocol {
    private final long expireMillions;
    private final String root;
    private final String tokenElement;
    private final String keyElement;

    public Protocol(int expireMillions) {
        this.expireMillions = expireMillions;
        root = IDUtils.get32UUID();
        tokenElement = IDUtils.get32UUID();
        keyElement = IDUtils.get32UUID();
    }

    public String toToken() {
        return DigestUtils.sha256Hex(root + tokenElement);
    }

    public String toValidKey() {
        return DigestUtils.sha256Hex(root + keyElement);
    }

}
