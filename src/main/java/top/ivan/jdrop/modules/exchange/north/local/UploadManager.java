package top.ivan.jdrop.modules.exchange.north.local;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import top.ivan.jdrop.modules.exchange.north.local.resource.FileResource;
import top.ivan.jdrop.modules.exchange.north.local.resource.TextResource;
import top.ivan.jdrop.modules.exchange.stable.dto.UploadResult;

import java.util.List;

@Slf4j
@Service
public class UploadManager {


    public Mono<UploadResult> uploadText(TextResource res) {
//        accessManager.isAccessible()
        return Mono.empty();
    }

    public Mono<UploadResult> uploadURL(TextResource res) {
        return Mono.empty();
    }

    public Mono<UploadResult> uploadImage(List<FileResource> res) {
        return Mono.empty();
    }

    public Mono<UploadResult> uploadFile(List<FileResource> res) {
        return Mono.empty();
    }

}
