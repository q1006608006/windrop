package top.ivan.windrop.ctl;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ZeroCopyHttpOutputMessage;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;

/**
 * @author Ivan
 * @description
 * @date 2021/3/1
 */
@RestController
@RequestMapping("/windrop/down")
public class DownloadController {

    @GetMapping(value = "t1", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Mono<Resource> get(ServerHttpRequest request, ServerHttpResponse response) throws IOException {
        Resource resource = new FileSystemResource("D:\\iosShare\\IMG_0285.jpeg");
        response.getHeaders().set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=test.ipa");
        response.getHeaders().setContentType(MediaType.APPLICATION_OCTET_STREAM);

        return Mono.just(resource);

//
//        List<HttpRange> ranges = request.getHeaders().getRange();
//        if (ranges.isEmpty()) {
//            ranges = Collections.singletonList(HttpRange.createByteRange(0));
//        }
//
//        response.getHeaders().setRange(Collections.singletonList(ranges.get(0)));
//        AsynchronousFileChannel channel = AsynchronousFileChannel.open(resource.getFile().toPath());
//
//        ResourceRegion range = ranges.get(0).toResourceRegion(resource);
//        return Flux.create(sink -> {
//            NettyDataBufferFactory dataBufferFactory = new NettyDataBufferFactory(new UnpooledByteBufAllocator(false));
//            DataBuffer buffer = dataBufferFactory.allocateBuffer((int) range.getCount());
//            ByteBuffer byteBuffer = buffer.asByteBuffer();
//            try {
//                channel.read(byteBuffer, range.getPosition()).get();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//            }
//            sink.next(buffer);
//            sink.complete();
//        });
    }

    @GetMapping(value = "t2", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Mono<Void> get2(ServerHttpResponse response) throws IOException {
        ZeroCopyHttpOutputMessage zeroCopyResponse = (ZeroCopyHttpOutputMessage) response;
        response.getHeaders().set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=test.ipa");
        response.getHeaders().setContentType(MediaType.APPLICATION_OCTET_STREAM);

        Resource res = new FileSystemResource("D:\\iosShare\\GoodNotes_5.6.28_@Zachary_Cracks( 14.0 ok) .ipa");
        File file = res.getFile();
        return zeroCopyResponse.writeWith(file, 0, file.length());
    }

}
