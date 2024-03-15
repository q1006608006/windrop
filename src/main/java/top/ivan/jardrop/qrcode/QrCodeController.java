package top.ivan.jardrop.qrcode;

import com.google.zxing.WriterException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import top.ivan.jardrop.common.cache.LimitCache;
import top.ivan.windrop.util.ConvertUtil;

import java.io.IOException;

/**
 * @author Ivan
 * @description 二维码生成控制器
 * @date 2021/3/26
 */
@Slf4j
@RestController
@RequestMapping("/jardrop/qr")
public class QrCodeController {
    // 二维码的宽度
    static final int WIDTH = 50;
    // 二维码的高度
    static final int HEIGHT = 50;
    // 二维码的格式
    static final String FORMAT = "png";

    private LimitCache<String> service;

    /**
     * 根据二维码ID返回二维码
     *
     * @param id       二维码ID
     * @param response response操作类
     * @return 二维码图流
     */
    @RequestMapping("{id}")
    public Mono<byte[]> getQrCode(@PathVariable String id, ServerHttpResponse response) throws CacheNotAccessableException {
        return Mono.just(service.getData(id)) //根据ID获取数据
                //设置返回媒体类型
                .doOnNext(v -> response.getHeaders().setContentType(MediaType.IMAGE_PNG))
                .flatMap(data -> {
                    try {
                        //生成二维码并返回
                        return Mono.just(ConvertUtil.toQrCode(data, WIDTH, HEIGHT, FORMAT));
                    } catch (WriterException | IOException e) {
                        return Mono.error(e);
                    }
                });
    }
}
