package top.ivan.windrop.ctl;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import top.ivan.windrop.WinDropConfiguration;
import top.ivan.windrop.bean.CommonResponse;
import top.ivan.windrop.ex.HttpClientException;
import top.ivan.windrop.ex.HttpServerException;
import top.ivan.windrop.svc.PersistUserService;
import top.ivan.windrop.svc.RandomAccessKeyService;
import top.ivan.windrop.svc.ResourceSharedService;
import top.ivan.windrop.util.IDUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    @Autowired
    private RandomAccessKeyService keyService;
    @Autowired
    private PersistUserService userService;

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
    public Mono<String> uploadModel(@RequestParam String key, @RequestParam String id, Model model) {
        return Mono.fromSupplier(() -> {
            try {
                return userService.findUser(id);
            } catch (IOException e) {
                throw new HttpServerException("数据服务异常");
            }
        }).doOnNext(user -> {
            if (!keyService.match(WinDropConfiguration.SWAP_GROUP, k -> DigestUtils.sha256Hex(user.getValidKey() + ";" + k).equals(key))) {
                throw new HttpClientException(HttpStatus.FORBIDDEN, "核验未通过");
            }
        }).map(user -> {
            model.addAttribute("hidden", keyService.getKey(WinDropConfiguration.FILE_UPLOAD_GROUP, 3 * 60));
            return "upload";
        });
    }

    @ResponseBody
    @PostMapping("/upload")
    public Mono<CommonResponse> fileUpload(@RequestPart("file") Mono<FilePart> mono, @RequestPart("hidden") String hidden) {
        return mono.doFirst(() -> {
            if (!keyService.match(WinDropConfiguration.FILE_UPLOAD_GROUP, k -> k.equals(hidden))) {
                throw new HttpClientException(HttpStatus.FORBIDDEN, "未知来源的请求");
            }
        }).doOnNext(fp -> {
            String fn = fp.filename();
            Path p = Paths.get(WinDropConfiguration.UPLOAD_FILES_PATH, fn);
            fp.transferTo(p);
        }).map(fp -> CommonResponse.success("上传成功"));
//        System.out.println("receive hidden: " + hidden);
//        return mono.doOnNext(p -> System.out.println(p.filename())).map(f -> Collections.singletonMap("t", "1"));
    }
}
