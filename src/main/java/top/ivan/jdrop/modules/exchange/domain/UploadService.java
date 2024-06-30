package top.ivan.jdrop.modules.exchange.domain;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import top.ivan.jdrop.modules.exchange.south.port.ClipboardClient;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;

@Service
public class UploadService {

    @Resource
    private ClipboardClient clipboardClient;

    public Mono<Void> uploadText(String text) {
        return Mono.fromRunnable(() -> clipboardClient.setText(text));
    }

    public Mono<Void> uploadURL(String url) {
        return Mono.fromRunnable(() -> clipboardClient.setUrl(url));
    }

    public Mono<Void> uploadFile(List<File> files) {
        return Mono.fromRunnable(() -> clipboardClient.setFiles(files));
    }

    public Mono<Void> uploadImage(List<File> imageFiles) {
        return Mono.fromRunnable(() -> clipboardClient.setImageFiles(imageFiles));
    }
}
