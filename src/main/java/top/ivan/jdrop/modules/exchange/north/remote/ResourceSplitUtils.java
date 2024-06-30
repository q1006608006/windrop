package top.ivan.jdrop.modules.exchange.north.remote;

import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import top.ivan.jdrop.framework.constant.MediaType;
import top.ivan.jdrop.framework.system.io.FileFactory;
import top.ivan.jdrop.modules.exchange.north.local.resource.FileResource;
import top.ivan.jdrop.modules.exchange.north.local.resource.MultiPartFileResource;
import top.ivan.jdrop.modules.exchange.north.local.resource.TextResource;

public class ResourceSplitUtils {

    public static Flux<FileResource> toFileResources(ServerWebExchange exchange, MediaType type, FileFactory factory) {
        return exchange.getMultipartData().flatMapMany(map -> Flux.fromStream(
                map.entrySet().stream()
                        .flatMap(e -> e.getValue().stream())
                        .map(part -> new MultiPartFileResource((FilePart) part, type, factory))
        ));
    }

    public static Mono<TextResource> toTextResources(ServerWebExchange exchange, MediaType type) {
        return exchange.getMultipartData().map(map -> map.values().stream()
                .flatMap(e -> e.stream())
                .findAny().get()
        ).map(p -> new TextResource((FormFieldPart) p, type));
    }

}
