package top.ivan.windrop.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import reactor.core.publisher.Mono;

/**
 * @author Ivan
 * @description
 * @date 2021/3/22
 */
public class HttpClientException extends HttpClientErrorException {
    public HttpClientException(HttpStatus statusCode, String message) {
        super(message, statusCode, "", null, null, null);
    }

    public static <T> Mono<T> badRequest(String message) {
        return Mono.error(new HttpClientException(HttpStatus.BAD_REQUEST, message));
    }

    public static <T> Mono<T> forbidden(String message) {
        return Mono.error(new HttpClientException(HttpStatus.FORBIDDEN, message));
    }
}
