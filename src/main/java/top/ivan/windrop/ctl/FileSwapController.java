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
import top.ivan.windrop.svc.RandomAccessKeyService;
import top.ivan.windrop.svc.ResourceSharedService;
import top.ivan.windrop.util.ConvertUtil;
import top.ivan.windrop.util.IDUtil;
import top.ivan.windrop.verify.VerifyIP;
import top.ivan.windrop.verify.WebHandler;

import java.io.File;
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
    public static final String FILE_UPLOAD_GROUP = "FILE_UPLOAD";

    public static final int UPLOAD_TIMEOUT = Integer.MAX_VALUE;

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
                    log.error("server io exception", e);
                    throw new HttpServerException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
                }
            }
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
        return Mono.just(ApplyResponse.success(keyService.getKey(prepareKey(request.getId(), FILE_UPLOAD_APPLY_GROUP), 30)));
    }

    @GetMapping("upload/{key}")
    @VerifyIP
    public Mono<String> uploadModel(@RequestParam String id, @PathVariable String key) {
        AccessUser user = prepareUser(id);

        return WebHandler.ip()
                .doOnNext(ip -> valid(key, user, ip))
                .then(WebHandler.session())
                .doOnNext(s -> s.getAttributes().put("user", user))
                .thenReturn("upload");
    }

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

    @ResponseBody
    @PostMapping("test")
    @VerifyIP
    public CommonResponse test(@RequestParam("id") String id) {
        return CommonResponse.success("hh");
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

    private void valid(String key, AccessUser user, String ip) {
        if (!keyService.match(prepareKey(user.getId(), FILE_UPLOAD_APPLY_GROUP), k -> DigestUtils.sha256Hex(user.getValidKey() + ";" + ip + ";" + k).equals(key))) {
            //出于安全角度考量，在没有合适的验证方式前，使用本功能时不建议使用代理网络
            //方案2：快捷指令端，在扫码连接时指定被代理IP，但该方法对于不固定分配IPV4地址的网络或其他复杂网络情况可能无法奏效，且仍然存在篡改内容的风险
            throw new HttpClientException(HttpStatus.FORBIDDEN, "核验未通过，若您使用代理网络，请关闭代理后重试");
        }
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

}
