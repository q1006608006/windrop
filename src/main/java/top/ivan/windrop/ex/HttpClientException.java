package top.ivan.windrop.ex;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

/**
 * @author Ivan
 * @description
 * @date 2021/3/22
 */
public class HttpClientException extends HttpClientErrorException {
    public HttpClientException(HttpStatus statusCode, String message) {
        super(message, statusCode, "", null, null, null);
    }
}
