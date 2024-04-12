package top.ivan.jdrop.exchange.swap.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import top.ivan.jdrop.clipboard.application.ClipboardService;
import top.ivan.jdrop.common.constant.OperateType;
import top.ivan.jdrop.exchange.common.vo.ExchangeVO;
import top.ivan.jdrop.exchange.common.vo.UploadData;
import top.ivan.jdrop.exchange.security.application.AccessManageService;
import top.ivan.jdrop.exchange.swap.application.dto.UploadResult;

import static top.ivan.jdrop.exchange.swap.application.assembly.ExchangeToSetterAssembly.toSetter;

@Service
public class UploadService {

    @Autowired
    private AccessManageService accessManager;

    @Autowired
    private ClipboardService clipboardService;


    public Mono<UploadResult> doUpload(ExchangeVO<UploadData> exchange) {
        return accessManager.tryAccess(exchange, OperateType.PUSH)
                .filter(result -> true)
                .flatMap(ignore -> clipboardService.setClipboard(toSetter(exchange.getMediaType(), exchange.getData())))
                .transform(UploadService::convertToResult);
    }


    private static Mono<UploadResult> convertToResult(Mono<?> any) {
        return any.thenReturn(UploadResult.SUCCESS)
                .defaultIfEmpty(UploadResult.FAILED);
    }

}
