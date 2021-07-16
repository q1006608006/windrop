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
 * @description 二维码生成控制器
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

    /**
     * 根据二维码ID返回二维码
     *
     * @param id       二维码ID
     * @param response response操作类
     * @return 二维码图流
     */
    @RequestMapping("{id}")
    public Mono<byte[]> getQrCode(@PathVariable String id, ServerHttpResponse response) {
        return Mono.fromSupplier(() -> {
            try {
                //根据ID获取数据
                String data = service.getData(id);
                //设置返回媒体类型
                response.getHeaders().setContentType(MediaType.IMAGE_PNG);
                //生成二维码并返回
                return ConvertUtil.getQrCodeImageBytes(data, WIDTH, HEIGHT, FORMAT);
            } catch (WriterException | IOException e) {
                throw new HttpClientException(HttpStatus.INTERNAL_SERVER_ERROR, "二维码生成失败");
            } catch (CacheNotFoundException e) {
                throw new HttpClientException(HttpStatus.NOT_FOUND, "未知来源的请求");
            } catch (CacheNotAccessException e) {
                throw new HttpClientException(HttpStatus.OK, "二维码使用次数超过上限");
            } catch (CacheTimeoutException e) {
                throw new HttpClientException(HttpStatus.REQUEST_TIMEOUT, "二维码已失效");
            }
        });
    }
}
