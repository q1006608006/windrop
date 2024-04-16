package top.ivan.jdrop.exchange.swap.userinterface;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import top.ivan.jdrop.common.module.ResponseVO;
import top.ivan.jdrop.exchange.common.vo.ExchangeVO;
import top.ivan.jdrop.exchange.common.vo.UploadData;
import top.ivan.jdrop.exchange.swap.application.UploadService;
import top.ivan.jdrop.exchange.common.vo.ExchangeResult;

/**
 * @author Ivan
 * @since 2024/03/29 00:22
 */
@RestController("jdrop")
public class UploadController {
    @Autowired
    private UploadService service;

    @RequestMapping("upload")
    public Mono<ResponseVO<ExchangeResult>> upload(Mono<ExchangeVO<UploadData>> mono) {
        return mono.flatMap(service::doUpload)
                .map(ResponseVO::success);
    }
}
