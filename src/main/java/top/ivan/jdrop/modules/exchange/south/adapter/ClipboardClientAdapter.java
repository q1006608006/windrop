package top.ivan.jdrop.modules.exchange.south.adapter;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import top.ivan.jdrop.framework.system.io.clipboard.ClipboardUtils;
import top.ivan.jdrop.modules.exchange.south.port.ClipboardClient;
import top.ivan.windrop.system.clipboard.ClipType;

import java.io.File;
import java.util.List;

@Component
public class ClipboardClientAdapter implements ClipboardClient {

    @Override
    public Mono<Void> setText(String text) {
        return from(() -> ClipboardUtils.setText(text));
    }

    @Override
    public Mono<Void> setUrl(String url) {
        return from(() -> ClipboardUtils.setUrl(url));
    }

    @Override
    public Mono<Void> setFiles(List<File> files) {
        return from(() -> ClipboardUtils.setFiles(files));

    }

    @Override
    public Mono<Void> setImageFiles(List<File> images) {
        return from(() -> ClipboardUtils.setImageFiles(images));
    }

    @Override
    public Mono<ClipType> getClipType() {
        return null;
    }

    @Override
    public <T> Mono<T> getClipboardData(ClipType type) {
        return null;
    }

    private Mono<Void> from(Runnable run) {
        return Mono.fromRunnable(run);
    }
}
