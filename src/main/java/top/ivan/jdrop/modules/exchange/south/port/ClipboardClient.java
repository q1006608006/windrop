package top.ivan.jdrop.modules.exchange.south.port;

import reactor.core.publisher.Mono;
import top.ivan.windrop.system.clipboard.ClipType;

import java.io.File;
import java.util.List;

public interface ClipboardClient {

    Mono<Void> setText(String text);

    Mono<Void> setUrl(String url);

    Mono<Void> setFiles(List<File> file);

    Mono<Void> setImageFiles(List<File> images);

    Mono<ClipType> getClipType();

    <T> Mono<T> getClipboardData(ClipType type);

}
