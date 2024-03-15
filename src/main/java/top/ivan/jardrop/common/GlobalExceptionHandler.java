package top.ivan.jardrop.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 处理未捕获的异常
 */
@Slf4j
//@ControllerAdvice
public class GlobalExceptionHandler {
    private static final MediaType MEDIA_TYPE = new MediaType("application", "json", StandardCharsets.UTF_8);

    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<?> exceptionHandler(Exception e) {
        Map<String, Object> data = new HashMap<>();
        data.put("success", false);
        data.put("message", e.getMessage() == null ? e.getClass().getName() : e.getMessage());

        HttpStatus status;
        if (e instanceof HttpClientErrorException) {
            status = ((HttpClientErrorException) e).getStatusCode();
            log.error(e.getMessage());
        } else if (e instanceof HttpServerErrorException) {
            status = ((HttpServerErrorException) e).getStatusCode();
            log.error("系统异常", e);
        } else if (e instanceof HttpStatusCodeException) {
            status = ((HttpStatusCodeException) e).getStatusCode();
            log.error("未知异常", e);
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            log.error("未知异常", e);
        }

        return ResponseEntity.status(status).contentType(MEDIA_TYPE).body(data);
    }
}