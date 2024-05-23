package top.ivan.jdrop.modules.exchange.north.remote;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import top.ivan.jdrop.framework.constant.MediaType;
import top.ivan.jdrop.framework.constant.Operate;
import top.ivan.jdrop.framework.event.JDropEventBus;
import top.ivan.jdrop.framework.exception.IllegalClientRequestException;
import top.ivan.jdrop.framework.model.ResponseVO;
import top.ivan.jdrop.modules.device.northbound.local.AccessManager;
import top.ivan.jdrop.modules.device.stable.module.dto.AccessApplyDTO;
import top.ivan.jdrop.modules.device.stable.module.vo.AccessInfoVO;
import top.ivan.jdrop.modules.exchange.north.local.UploadManager;
import top.ivan.jdrop.modules.exchange.north.local.resource.Base64TextResourceWrapper;
import top.ivan.jdrop.modules.exchange.north.local.resource.ExchangeResource;
import top.ivan.jdrop.modules.exchange.north.local.resource.FileFactory;
import top.ivan.jdrop.modules.exchange.stable.dto.UploadResult;
import top.ivan.jdrop.modules.exchange.stable.vo.UploadInfoVO;

import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("jdrop/upload")
public class UploadController {

    private final UploadManager uploadManager;

    private final AccessManager accessManager;

    private final JDropEventBus eventBus;

    public UploadController(UploadManager uploadManager, AccessManager accessManager, JDropEventBus eventBus) {
        this.uploadManager = uploadManager;
        this.accessManager = accessManager;
        this.eventBus = eventBus;
    }

    /**
     * request for new upload access
     * <p>
     * digest for multi-part-file: String.join("&&", ...res-sha256)
     *
     * @param mono mono of Upload Info
     * @return
     */
    @GetMapping
    public Mono<ResponseVO<AccessApplyDTO>> apply(Mono<UploadInfoVO> mono) {
        return mono.flatMap(info -> accessManager.accessApply(new AccessInfoVO(info.getDeviceId(),
                        Operate.UPLOAD,
                        info.getMediaType(),
                        info.getDigests().stream().collect(Collectors.joining("&&")))))
                .map(ResponseVO::success);
    }

    @PostMapping("{code}")
    public Mono<ResponseVO<UploadResult>> upload(ServerWebExchange req, @PathVariable String code) {
        return checkAccess(code)
                .flatMap(info -> info.getMediaType().isTextType() ?
                        uploadTextResource(req, info) :
                        uploadStreamResource(req, info))
                .map(ResponseVO::success)
                .doOnSuccess(v -> eventBus.publish(new UploadEvent()));
    }

    private Mono<AccessInfoVO> checkAccess(String ticket) {
        return accessManager.isAccessible(ticket)
                .flatMap(pass -> pass ?
                        accessManager.getAccessInfo(ticket) :
                        Mono.error(() -> new IllegalClientRequestException("禁止访问")));
    }

    private Mono<UploadResult> uploadTextResource(ServerWebExchange req, AccessInfoVO info) {
        return ResourceSplitUtils.toTextResources(req, info.getMediaType())
                .map(Base64TextResourceWrapper::new)
                .flatMap(res -> valid(Flux.just(res), info).thenReturn(res))
                .flatMap(res -> res.type() == MediaType.TEXT ?
                        uploadManager.uploadText(res) :
                        uploadManager.uploadURL(res));
    }

    private Mono<UploadResult> uploadStreamResource(ServerWebExchange req, AccessInfoVO info) {
        return ResourceSplitUtils.toFileResources(req, info.getMediaType(), FileFactory.USER_DOWNLOAD_RANDOM_NAME_FACTORY)
                .transform(flux -> valid(flux, info).thenMany(flux))
                .doOnNext(res -> res.getFile()
                        .renameTo(FileFactory.USER_DOWNLOAD_FACTORY.crateFile(res.getFile().getName())))
                .collect(Collectors.toList())
                .flatMap(list -> info.getMediaType() == MediaType.FILE ?
                        uploadManager.uploadFile(list) :
                        uploadManager.uploadImage(list));
    }

    private <T extends ExchangeResource> Mono<Void> valid(Flux<T> resources, AccessInfoVO info) {
        return resources.reduce(info.getDigest(), (str, res) -> str.replaceFirst(res.sha256(), ""))
                .map(str -> str.replace("&&", ""))
                .flatMap(str -> "".equals(str) ?
                        Mono.empty() :
                        Mono.error(() -> new IllegalClientRequestException("上传资源校验异常")));
    }

}
