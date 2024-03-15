package top.ivan.windrop.security.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import top.ivan.windrop.common.OperateType;
import top.ivan.windrop.common.ResourceType;
import top.ivan.windrop.util.ConvertUtil;

/**
 * @author Ivan
 * @since 2023/09/08 11:09
 */
@Data
public class UserAuthEntity {

    /**
     * userId
     */
    private String id;

    /**
     * [PUSH, PULL]
     */
    private OperateType operate;

    /**
     * [FILE, TEXT, IMAGE, UNKNOWN]
     */
    private ResourceType type = ResourceType.UNKNOWN;

    /**
     * content
     */
    private String content;

    /**
     * sha256(ip,key,content)
     */
    private String sign;

    /**
     * resourceName
     */
    private String resourceName;

    /**
     * name for confirm
     */
    private String showName;

    public boolean isPull() {
        return OperateType.PULL == operate;
    }


    @Setter(AccessLevel.NONE)
    private byte[] bytes;

    public byte[] toBytes() {
        if (bytes == null && content != null) {
            bytes = ConvertUtil.decodeBase64(content);
        }
        return bytes;
    }

}
