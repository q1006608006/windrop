package top.ivan.jdrop.modules.device.domain.entity;

import lombok.Data;
import org.apache.commons.codec.digest.DigestUtils;
import top.ivan.jdrop.framework.util.IDUtils;
import top.ivan.jdrop.modules.device.stable.module.vo.DeviceBindVO;

import java.util.Objects;

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


    public long nextExpireTime() {
        return System.currentTimeMillis() + expireMillions;
    }

    public boolean checkSign(DeviceBindVO bind) {
        return Objects.equals(
                DigestUtils.sha256Hex(String.join(";", bind.getId(), bind.getName(), tokenElement)),
                bind.getSign()
        );
    }

    public String getToken() {
        return DigestUtils.sha256Hex(String.join(";", keyRoot, tokenElement));
    }

    public String getValidKey() {
        return DigestUtils.sha256Hex(String.join(";", keyRoot, keyElement));
    }

    public static String buildToken(String keyRoot, String tokenElement) {
        return DigestUtils.sha256Hex(String.join(";", keyRoot, tokenElement));
    }

}
