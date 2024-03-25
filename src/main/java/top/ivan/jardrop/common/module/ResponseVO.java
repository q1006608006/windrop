package top.ivan.jardrop.common.module;

import lombok.Data;

/**
 * @author Ivan
 * @since 2024/01/30 10:56
 */
@Data
public class ResponseVO<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> ResponseVO<T> success(T data) {
        return success(data, null);
    }

    public static <T> ResponseVO<T> success(T data, String message) {
        ResponseVO<T> r = new ResponseVO<>();
        r.setSuccess(true);
        r.setMessage(message);
        r.setData(data);
        return r;
    }

    public static <T> ResponseVO<T> fail(String message) {
        return fail(message, null);
    }

    public static <T> ResponseVO<T> fail(String message, T data) {
        ResponseVO<T> f = new ResponseVO<>();
        f.setSuccess(false);
        f.setMessage(message);
        f.setData(data);
        return f;
    }
}
