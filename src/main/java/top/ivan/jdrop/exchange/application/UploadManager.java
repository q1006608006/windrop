package top.ivan.jdrop.exchange.application;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import top.ivan.jdrop.framework.system.clipboard.application.ClipboardService;
import top.ivan.jdrop.framework.constant.OperateType;
import top.ivan.jdrop.framework.module.ExchangeVO;
import top.ivan.jdrop.exchange.userinterface.vo.upload.UploadData;
import top.ivan.jdrop.exchange.application.assembly.ExchangeToSetterAssembly;
import top.ivan.jdrop.exchange.application.dto.UploadResult;
import top.ivan.jdrop.user.application.AccessManager;

import javax.annotation.Resource;

@Service
public class UploadManager {

    @Resource
    private AccessManager accessManager;

    @Resource
    private ClipboardService clipboardService;

    private final ExchangeToSetterAssembly setterAssembly = new ExchangeToSetterAssembly();


    public Mono<UploadResult> doUpload(ExchangeVO<UploadData> exchange) {
        return accessManager.tryAccess(exchange, OperateType.PUSH)
                .filter(result -> true)
                .then(sort(exchange))
//                .flatMap(ignore -> clipboardService.setClipboard(toSetter(exchange.getMediaType(), exchange.getData())))
                .transform(UploadManager::convertToResult);
    }


    private static Mono<UploadResult> convertToResult(Mono<?> any) {
        return any.thenReturn(UploadResult.SUCCESS)
                .defaultIfEmpty(UploadResult.FAILED);
    }

    private Mono<Void> sort(ExchangeVO<UploadData> exchange) {
        switch (exchange.getMediaType()) {
            case URL:
                System.out.println("url");
                break;
            case FILE:
                System.out.println("file");
                break;
            case TEXT:
                System.out.println("text");
                break;
            case IMAGE:
                break;
        }

        return Mono.empty();
    }

}
