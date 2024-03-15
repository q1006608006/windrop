package top.ivan.jardrop.common.exception;

import org.springframework.http.HttpStatus;

/**
 * @author Ivan
 * @description
 * @date 2021/3/29
 */
public class LengthTooLargeException extends HttpClientException {

    public LengthTooLargeException() {
        super(HttpStatus.FORBIDDEN, "传输的数据过长");
    }

    public LengthTooLargeException(String msg) {
        super(HttpStatus.FORBIDDEN, msg);
    }
}
