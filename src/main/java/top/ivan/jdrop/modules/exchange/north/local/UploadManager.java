package top.ivan.jdrop.modules.exchange.north.local;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import top.ivan.jdrop.modules.exchange.domain.UploadService;
import top.ivan.jdrop.modules.exchange.north.local.resource.FileResource;
import top.ivan.jdrop.modules.exchange.north.local.resource.TextResource;
import top.ivan.jdrop.modules.exchange.stable.dto.UploadResult;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UploadManager {

    @Autowired
    private UploadService service;


    public Mono<UploadResult> uploadText(TextResource res) {
        return wrapResult(service.uploadText(res.getContent()));
    }

    public Mono<UploadResult> uploadURL(TextResource res) {
        return wrapResult(service.uploadURL(res.getContent()));
    }

    public Mono<UploadResult> uploadImage(List<FileResource> res) {
        return service.uploadImage(res.stream()
                        .map(FileResource::getFile)
                        .collect(Collectors.toList()))
                .transform(this::wrapResult);

    }

    public Mono<UploadResult> uploadFile(List<FileResource> res) {
        return service.uploadFile(res.stream()
                        .map(FileResource::getFile)
                        .collect(Collectors.toList()))
                .transform(this::wrapResult);
    }

    private Mono<UploadResult> wrapResult(Mono<?> mono) {
        return mono.thenReturn(UploadResult.SUCCESS)
                .onErrorResume(e -> {
                    log.error("do upload failed", e);
                    return Mono.just(UploadResult.FAILED);
                });
    }
}
