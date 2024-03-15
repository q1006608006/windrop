package top.ivan.windrop.bean;

import lombok.Data;
import top.ivan.windrop.common.ResourceType;
import top.ivan.windrop.security.domain.UserAuthEntity;

/**
 * @author Ivan
 * @description 推送数据
 * @date 2021/1/14
 */
@Data
public class WindropRequest extends UserAuthEntity {

    /**
     * 推送的文件名称
     */
    private String filename;

    /**
     * 推送的文件后缀名称
     */
    private String fileSuffix;

    /**
     * 推送数据类型
     */
    private ResourceType type;

}
