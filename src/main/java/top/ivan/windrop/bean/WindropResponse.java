package top.ivan.windrop.bean;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Ivan
 * @description
 * @date 2021/1/14
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class WindropResponse extends CommonResponse {
    private long serverUpdateTime;
    private String type;
    private String data;
    private String fileName;
    private String sign;
    private String resourceId;

    public WindropResponse() {
        setSuccess(true);
    }

}
