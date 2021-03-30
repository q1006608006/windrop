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
public class ApplyResponse extends CommonResponse{
    private String accessKey;

    public static ApplyResponse success(String key) {
        ApplyResponse response = new ApplyResponse();
        response.setAccessKey(key);
        response.setSuccess(true);
        return response;
    }

    public static ApplyResponse failed(String message) {
        ApplyResponse response = new ApplyResponse();
        response.setMessage(message);
        response.setSuccess(false);
        return response;
    }
}
