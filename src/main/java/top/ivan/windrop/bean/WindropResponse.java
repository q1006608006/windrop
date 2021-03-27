package top.ivan.windrop.bean;

import lombok.Data;

/**
 * @author Ivan
 * @description
 * @date 2021/1/14
 */
@Data
public class WindropResponse {
    private long serverUpdateTime;
    private String type;
    private String data;
    private String fileName;
    private String sign;
}
