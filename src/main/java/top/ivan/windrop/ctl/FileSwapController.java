package top.ivan.windrop.ctl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
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

import java.io.File;
import java.io.IOException;

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
    @GetMapping(value = "download/{key}")
    public Mono<Resource> download(@PathVariable String key, ServerHttpResponse response) {
        Resource res = resourceSharedService.findResource(key);
        if (null == res) {
            return Mono.error(new HttpClientException(HttpStatus.NOT_FOUND, "not found"));
        }

        return Mono.just(res).doFirst(() -> {
            if (res.isFile()) {
                File file;
                try {
                    file = res.getFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                response.getHeaders().set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName());
            }
        });
    }

    @GetMapping("/upload")
    public Mono<String> ss(Model model) {
        model.addAttribute("name","ivan");
        model.addAttribute("city","FJ.QZ");
        return Mono.create(sink -> sink.success("index"));
    }
}
