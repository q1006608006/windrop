package top.ivan.windrop.bean;

import lombok.Data;

/**
 * @author Ivan
 * @description
 * @date 2021/3/29
 */
@Data
public class CommonResponse {
    protected boolean success;
    protected String message;

    public static CommonResponse success(String msg) {
        CommonResponse resp = new CommonResponse();
        resp.success = true;
        resp.message = msg;
        return resp;
    }

    public static CommonResponse failed(String msg) {
        CommonResponse resp = new CommonResponse();
        resp.success = false;
        resp.message = msg;
        return resp;
    }
}
