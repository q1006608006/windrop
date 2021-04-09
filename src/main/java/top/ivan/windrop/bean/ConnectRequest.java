package top.ivan.windrop.bean;

import lombok.Data;

/**
 * @author Ivan
 * @description
 * @date 2021/4/2
 */
@Data
public class ConnectRequest {
    private String sign;
    private String deviceId;
    private String data;
    private String locate;
}
