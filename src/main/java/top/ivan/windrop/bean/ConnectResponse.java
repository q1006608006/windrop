package top.ivan.windrop.bean;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Ivan
 * @description
 * @date 2021/4/2
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ConnectResponse extends CommonResponse{
    private String id;
    private String validKey;
}
