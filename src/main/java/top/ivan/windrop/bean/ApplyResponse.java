package top.ivan.windrop.bean;

import lombok.Data;

/**
 * @author Ivan
 * @description
 * @date 2021/1/14
 */
@Data
public class ApplyResponse {
    private String accessKey;
    private boolean success;
    private String message;

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
