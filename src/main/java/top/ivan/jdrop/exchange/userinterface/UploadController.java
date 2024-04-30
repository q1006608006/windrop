package top.ivan.jdrop.exchange.userinterface;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import top.ivan.jdrop.framework.module.ResponseVO;
import top.ivan.jdrop.framework.module.ExchangeVO;
import top.ivan.jdrop.exchange.userinterface.vo.upload.UploadData;
import top.ivan.jdrop.exchange.application.UploadManager;
import top.ivan.jdrop.framework.module.ExchangeResult;

/**
 * @author Ivan
 * @since 2024/03/29 00:22
 */
@RestController("jdrop")
public class UploadController {
    @Autowired
    private UploadManager service;

    @RequestMapping("upload")
    public Mono<ResponseVO<ExchangeResult>> upload(Mono<ExchangeVO<UploadData>> mono) {
        return mono.flatMap(service::doUpload)
                .map(ResponseVO::success);
    }
}
