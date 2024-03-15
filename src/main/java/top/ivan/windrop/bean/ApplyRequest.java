package top.ivan.windrop.bean;

import lombok.Data;
import top.ivan.windrop.security.domain.UserAuthEntity;

/**
 * @author Ivan
 * @description 请求数据
 * @date 2021/3/5
 */
@Data
public class ApplyRequest extends UserAuthEntity {


    /**
     * 当请求类型为file时的文件名
     */
    private String filename;

    /**
     * 数据大小
     */
    private String size;

}
