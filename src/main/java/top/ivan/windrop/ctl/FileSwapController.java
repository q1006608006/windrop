package top.ivan.windrop.ctl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import top.ivan.windrop.WinDropApplication;
import top.ivan.windrop.WinDropConfiguration;
import top.ivan.windrop.bean.AccessUser;
import top.ivan.windrop.bean.ApplyRequest;
import top.ivan.windrop.bean.ApplyResponse;
import top.ivan.windrop.bean.CommonResponse;
import top.ivan.windrop.ex.HttpClientException;
import top.ivan.windrop.ex.HttpServerException;
import top.ivan.windrop.svc.PersistUserService;
import top.ivan.windrop.svc.ResourceSharedService;
import top.ivan.windrop.svc.ValidService;
import top.ivan.windrop.util.ConvertUtil;
import top.ivan.windrop.util.IDUtil;
import top.ivan.windrop.verify.VerifyIP;
import top.ivan.windrop.verify.WebHandler;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;

/**
 * @author Ivan
 * @description
 * @date 2021/3/15
 */
@Slf4j
@Controller
@RequestMapping("/windrop/addition")
public class FileSwapController {

    static {
        File receiveDir = new File(WinDropConfiguration.UPLOAD_FILES_PATH);
        if (!receiveDir.exists() || !receiveDir.isDirectory()) {
            receiveDir.mkdir();
        }
    }

    public static final String FILE_UPLOAD_APPLY_GROUP = "FILE_UPLOAD_APPLY";

    @Autowired
    private ResourceSharedService resourceSharedService;

    @Autowired
    private PersistUserService userService;

    @Autowired
    private ValidService validService;

    @ResponseBody
    @GetMapping(value = "download/{key}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Mono<Resource> download(@PathVariable String key, ServerHttpResponse response) {
        Resource res = resourceSharedService.findResource(key);
        if (null == res) {
            return Mono.error(new HttpClientException(HttpStatus.NOT_FOUND, "资源不存在或已过期"));
        }
        return Mono.just(res).doOnNext(resource -> {
            if (resource.isFile()) {
                try {
                    String filename = Optional.ofNullable(resource.getFilename()).orElse(IDUtil.getShortUuid());
                    filename = new String(filename.getBytes(StandardCharsets.UTF_8), "ISO_8859_1");
                    response.getHeaders().set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
                } catch (IOException e) {
                    log.error("server io exception", e);
                    throw new HttpServerException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
                }
            }
        });
    }

    @RequestMapping(value = "download/{key}", method = {RequestMethod.HEAD})
    @ResponseBody
    public Mono<HttpHeaders> downloadTest(@PathVariable String key) {
        return WebHandler.ip().map(ip -> {
            HttpHeaders headers = new HttpHeaders();
            headers.add("accessible", String.valueOf(resourceSharedService.containsResource(key)));
            headers.add("source", ip);
            return headers;
        });
    }

    /**
     * 获取上传请求accessKey
     *
     * @return {@link ApplyResponse}
     */
    @ResponseBody
    @PostMapping("upload/apply")
    @VerifyIP
    public Mono<ApplyResponse> uploadApply(@RequestBody ApplyRequest request) {
        return validService.takeValidKey(prepareKey(request.getId(), FILE_UPLOAD_APPLY_GROUP), 90).map(ApplyResponse::success);
    }

    @GetMapping("upload/{key}")
    @VerifyIP
    public Mono<String> uploadModel(@RequestParam String id, @PathVariable String key) {
        AccessUser user = prepareUser(id);

        return valid(key, user)
                .then(WebHandler.session())
                .doOnNext(s -> s.getAttributes().put("user", user))
                .thenReturn("upload");
    }

/*
    @RequestMapping(value = "upload/{key}", method = RequestMethod.HEAD)
    @VerifyIP
    public Mono<HttpHeaders> uploadTest(@RequestParam String id, @PathVariable String key) {
        return WebHandler.ip()
                .map(ip -> {
                    AccessUser user = prepareUser(id);
                    boolean result = keyService.test(prepareKey(user.getId(), FILE_UPLOAD_APPLY_GROUP), k -> DigestUtils.sha256Hex(String.join(";", user.getValidKey(), ip, k)).equals(key));

                    HttpHeaders headers = new HttpHeaders();
                    headers.add("accessible", String.valueOf(result));
                    headers.add("source", ip);
                    return headers;
                });
    }
*/

    @ResponseBody
    @PostMapping("upload")
    @VerifyIP
    public Mono<CommonResponse> fileUpload(@RequestPart("file") Mono<FilePart> fpMono) {
        return fpMono.flatMap(fp -> {
            String fn = fp.filename();
            Path p = Paths.get(WinDropConfiguration.UPLOAD_FILES_PATH, fn);

            return WebHandler.session().map(s -> {
                AccessUser user = s.getAttribute("user");
                if (null == user) {
                    throw new HttpClientException(HttpStatus.FORBIDDEN, "未知来源的请求");
                }
                s.getAttributes().remove("user", user);
                return user;
            }).flatMap(user ->
                    fp.transferTo(p)
                            .then(WebHandler.ip())
                            .flatMap(ip -> Mono.just(CommonResponse.success("文件上传成功，请核对文件信息")).doFinally(s -> confirmFile(p.toFile(), user, ip)))
            );
        });
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

    private Mono<Boolean> valid(String sign, AccessUser user) {
        String group = prepareKey(user.getId(), FILE_UPLOAD_APPLY_GROUP);
        return validService.valid(group, sign, user)
                .doOnNext(success -> {
                    if (!success) throw new HttpClientException(HttpStatus.FORBIDDEN, "核验失败，请重新登陆");
                });
    }

    private String prepareKey(String id, String group) {
        return String.join("_", group, id);
    }

    private void confirmFile(File f, AccessUser user, String ip) {
        String md5;
        try {
            md5 = ConvertUtil.toHex(DigestUtils.digest(DigestUtils.getMd5Digest(), f));
        } catch (IOException e) {
            throw new HttpServerException(HttpStatus.INTERNAL_SERVER_ERROR, "服务器IO异常");
        }

        String msg = String.format("请确认是否接收文件: %s\n" +
                "来自设备: %s\n" +
                "大小: %s\n" +
                "md5摘要: %s", f.getName(), user.getAlias(), ConvertUtil.toShortSize(f.length()), md5);
        if (!WinDropApplication.WindropHandler.confirm("来自" + ip, msg)) {
            boolean success = f.delete();
            if (!success) {
                WinDropApplication.WindropHandler.alert("删除失败，请手动删除，文件路径: " + f.getAbsolutePath());
            }
        }
    }

    @RequestMapping(value = "test", method = {RequestMethod.HEAD})
    @ResponseBody
    public Mono<?> test(String id) {
        System.out.println(id);

        if ("1".equals(id)) {
            return Mono.just(Collections.singletonMap("test", id));
        } else {
            throw new HttpClientException(HttpStatus.NOT_FOUND, "no");
        }
    }
}
