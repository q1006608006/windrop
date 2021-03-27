package top.ivan.windrop.ctl;

import com.google.zxing.WriterException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import top.ivan.windrop.ex.CacheNotAccessException;
import top.ivan.windrop.ex.CacheNotFoundException;
import top.ivan.windrop.ex.CacheTimeoutException;
import top.ivan.windrop.ex.HttpClientException;
import top.ivan.windrop.svc.QrCodeControllerService;
import top.ivan.windrop.util.ConvertUtil;

import java.io.IOException;

/**
 * @author Ivan
 * @description
 * @date 2021/3/26
 */
@Slf4j
@RestController
@RequestMapping("/windrop/code")
public class QrCodeController {
    // 二维码的宽度
    static final int WIDTH = 300;
    // 二维码的高度
    static final int HEIGHT = 300;
    // 二维码的格式
    static final String FORMAT = "png";


    @Autowired
    private QrCodeControllerService service;

    @RequestMapping("{id}")
    public Mono<byte[]> getQrCode(@PathVariable String id, ServerHttpResponse response) {
        return Mono.fromSupplier(() -> {
            try {
                String data = service.getData(id);
                response.getHeaders().setContentType(MediaType.IMAGE_PNG);
                return ConvertUtil.getQrCodeImageBytes(data, WIDTH, HEIGHT, FORMAT);
            } catch (WriterException | IOException e) {
                throw new HttpClientException(HttpStatus.INTERNAL_SERVER_ERROR, "生成二维码失败");
            } catch (CacheNotFoundException e) {
                throw new HttpClientException(HttpStatus.NOT_FOUND, "未找到资源");
            } catch (CacheNotAccessException e) {
                throw new HttpClientException(HttpStatus.OK, "资源访问超过上限");
            } catch (CacheTimeoutException e) {
                throw new HttpClientException(HttpStatus.REQUEST_TIMEOUT, "资源已失效");
            }
        });
/*        try {
            String data = service.getData(id);
            byte[] qrCodeBody = ConvertUtil.getQrCodeImageBytes(data, WIDTH, HEIGHT, FORMAT);
            return ServerResponse.ok().header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE).bodyValue(qrCodeBody);
        } catch (WriterException | IOException e) {
            log.error("生成二维码失败", e);
            throw new HttpClientException(HttpStatus.INTERNAL_SERVER_ERROR, "生成二维码失败");
        } catch (CacheNotFoundException e) {
            throw new HttpClientException(HttpStatus.BAD_REQUEST, e.getMessage());
        }*/

    }
}
