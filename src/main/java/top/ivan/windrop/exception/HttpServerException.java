package top.ivan.windrop.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;

/**
 * @author Ivan
 * @description
 * @date 2021/3/22
 */
public class HttpServerException extends HttpServerErrorException {
    public HttpServerException(HttpStatus statusCode, String message) {
        super(message, statusCode, "", null, null, null);
    }

    public HttpServerException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, "", null, null, null);
    }
}
