package top.ivan.windrop.ctl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import top.ivan.windrop.WinDropConfiguration;
import top.ivan.windrop.bean.AccessUser;
import top.ivan.windrop.bean.ApplyResponse;
import top.ivan.windrop.bean.CommonResponse;
import top.ivan.windrop.ex.HttpClientException;
import top.ivan.windrop.ex.HttpServerException;
import top.ivan.windrop.svc.IPVerifier;
import top.ivan.windrop.svc.PersistUserService;
import top.ivan.windrop.svc.RandomAccessKeyService;
import top.ivan.windrop.svc.ResourceSharedService;
import top.ivan.windrop.util.IDUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static top.ivan.windrop.util.SpringUtil.getIP;

/**
 * @author Ivan
 * @description
 * @date 2021/3/15
 */
@Slf4j
@Controller
@RequestMapping("/windrop/addition")
public class FileSwapController {
    public static final String FILE_UPLOAD_APPLY_GROUP = "FILE_UPLOAD_APPLY";
    public static final String FILE_UPLOAD_GROUP = "FILE_UPLOAD";

    @Autowired
    private ResourceSharedService resourceSharedService;
    @Autowired
    private RandomAccessKeyService keyService;
    @Autowired
    private PersistUserService userService;
    @Autowired
    private IPVerifier ipVerifier;

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

    /**
     * 获取上传请求accessKey，非幂等，使用POST请求
     *
     * @param req http请求信息
     * @return {@link ApplyResponse}
     */
    @ResponseBody
    @PostMapping("upload/apply")
    public Mono<ApplyResponse> uploadApply(ServerHttpRequest req) {
//        verifyIp(req);
        return Mono.just(ApplyResponse.success(keyService.getKey(FILE_UPLOAD_APPLY_GROUP)));
    }

    @GetMapping("upload")
    public Mono<String> uploadModel(@RequestParam String key, @RequestParam String id, Mono<Model> mono) {
        AccessUser user = prepareUser(id);
        validApply(key, user);

        return mono.doOnNext(model -> model.addAttribute("hidden", keyService.getKey(FILE_UPLOAD_GROUP, 3 * 60)))
                .thenReturn("success");
    }

    @ResponseBody
    @PostMapping("upload/request")
    public Mono<ApplyResponse> uploadRequest(ServerHttpRequest req) {
        verifyIp(req);
        //todo
        return Mono.just(ApplyResponse.success(keyService.getKey(FILE_UPLOAD_APPLY_GROUP)));
    }

    @ResponseBody
    @PostMapping("upload")
    public Mono<CommonResponse> fileUpload(@RequestPart("file") Mono<FilePart> mono, @RequestPart("hidden") String hidden) {
        return mono.doFirst(() -> {
            if (!keyService.match(FILE_UPLOAD_GROUP, k -> k.equals(hidden))) {
                throw new HttpClientException(HttpStatus.FORBIDDEN, "未知来源的请求");
            }
        }).doOnNext(fp -> {
            String fn = fp.filename();
            Path p = Paths.get(WinDropConfiguration.UPLOAD_FILES_PATH, fn);
            fp.transferTo(p);
        }).thenReturn(CommonResponse.success("上传成功"));
    }

    private AccessUser prepareUser(String id) {
        try {
            AccessUser user = userService.findUser(id);
            if (null == user) {
                throw new HttpClientException(HttpStatus.UNAUTHORIZED, "未验证的设备(id: " + id + ")");
            }
            if (user.isExpired()) {
                throw new HttpClientException(HttpStatus.UNAUTHORIZED, "使用许可已过期");
            }
            return user;
        } catch (IOException e) {
            log.error("加载用户数据失败", e);
            throw new HttpServerException(HttpStatus.INTERNAL_SERVER_ERROR, "数据服务异常");
        }
    }

    private void validApply(String key, AccessUser user) {
        if (!keyService.match(FILE_UPLOAD_APPLY_GROUP, k -> DigestUtils.sha256Hex(user.getValidKey() + ";" + k).equals(key))) {
            throw new HttpClientException(HttpStatus.FORBIDDEN, "核验未通过");
        }
    }

    private void verifyIp(ServerHttpRequest request) {
        String ip = getIP(request);
        if (!ipVerifier.accessible(ip)) {
            log.info("unavailable ip: {}", ip);
            throw new HttpClientException(HttpStatus.FORBIDDEN, "未授予白名单");
        }
    }
}
