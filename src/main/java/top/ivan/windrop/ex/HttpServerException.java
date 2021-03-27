package top.ivan.windrop.ex;

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
}
