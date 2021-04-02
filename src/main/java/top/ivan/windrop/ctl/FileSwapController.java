package top.ivan.windrop.ctl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Mono;
import top.ivan.windrop.ex.HttpClientException;
import top.ivan.windrop.svc.ResourceSharedService;
import top.ivan.windrop.util.IDUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * @author Ivan
 * @description
 * @date 2021/3/15
 */
@Controller
@RequestMapping("/windrop/addition")
public class FileSwapController {

    @Autowired
    private ResourceSharedService resourceSharedService;

    @ResponseBody
    @GetMapping(value = "download/{key}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Mono<Resource> download(@PathVariable String key, ServerHttpResponse response) {
        Resource res = resourceSharedService.findResource(key);
        if (null == res) {
            return Mono.error(new HttpClientException(HttpStatus.NOT_FOUND, "not found"));
        }
        return Mono.just(res).doOnNext(resource -> {
            if (resource.isFile()) {
                try {
                    String filename = Optional.ofNullable(resource.getFilename()).orElse(IDUtil.getShortUuid());
                    filename = new String(filename.getBytes(StandardCharsets.UTF_8), "ISO_8859_1");
                    response.getHeaders().set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @GetMapping("/upload")
    public Mono<String> ss(Model model) {
        model.addAttribute("name", "ivan");
        model.addAttribute("city", "FJ.QZ");
        return Mono.create(sink -> sink.success("index"));
    }
}
