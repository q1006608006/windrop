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
import top.ivan.windrop.util.InitialResource;
import top.ivan.windrop.verify.VerifyIP;
import top.ivan.windrop.verify.WebHandler;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * @author Ivan
 * @description 大文件上传下载控制器
 * @date 2021/3/15
 */
@Slf4j
@Controller
@RequestMapping("/windrop/addition")
public class FileSwapController {

    static {
        // 创建文件储存目录
        File receiveDir = new File(WinDropConfiguration.UPLOAD_FILES_PATH);
        if (!receiveDir.exists() || !receiveDir.isDirectory()) {
            receiveDir.mkdir();
        }
    }

    /**
     * 文件上传随机验证码组
     */
    public static final String FILE_UPLOAD_APPLY_GROUP = "FILE_UPLOAD_APPLY";

    /**
     * 资源共享服务
     */
    @Autowired
    private ResourceSharedService resourceSharedService;

    /**
     * 用户服务
     */
    @Autowired
    private PersistUserService userService;

    /**
     * 核验服务
     */
    @Autowired
    private ValidService validService;

    /**
     * 下载资源
     *
     * @param key      资源ID
     * @param response 请求信息
     * @return 对应资源
     */
    @ResponseBody
    @GetMapping(value = "download/{key}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Mono<Resource> download(@PathVariable String key, ServerHttpResponse response) {
        return Mono.just(resourceSharedService.findResource(key))
                .flatMap(r -> r instanceof InitialResource ?
                        Mono.fromSupplier(() -> {
                            try {
                                return ((InitialResource) r).getResource();
                            } catch (Exception e) {
                                log.error("initialize resource failed", e);
                                throw new HttpServerException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
                            }
                        })
                        : Mono.justOrEmpty(r)
                )
                .switchIfEmpty(Mono.error(() -> new HttpClientException(HttpStatus.NOT_FOUND, "资源不存在或已过期")))
                .doOnNext(resource -> {
                    if (resource.isFile()) {
                        String filename = Optional.ofNullable(resource.getFilename()).orElse(IDUtil.getShortUuid());
                        filename = new String(filename.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
                        response.getHeaders().set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
                    }
                });
    }

    /**
     * 获取上传请求accessKey
     *
     * @param reqMono 申请请求
     * @return accessKey
     */
    @ResponseBody
    @PostMapping("upload/apply")
    @VerifyIP
    public Mono<ApplyResponse> uploadApply(@RequestBody Mono<ApplyRequest> reqMono) {
        return reqMono.map(req -> prepareKey(req.getId(), FILE_UPLOAD_APPLY_GROUP))
                .map(group -> ApplyResponse.success(validService.getValidKey(group, 90)));
    }

    /**
     * 访问下载页面
     *
     * @param id  用户id
     * @param key 验证钥（签名）
     * @return 下载页面
     */
    @GetMapping("upload/{key}")
    @VerifyIP
    public Mono<String> uploadModel(@RequestParam String id, @PathVariable String key) {
        AccessUser user = prepareUser(id);

        return valid(key, user)
                .then(WebHandler.session())
                .doOnNext(s -> s.getAttributes().put("user", user))
                .thenReturn("upload");
    }

    /**
     * 上传文件
     *
     * @param fpMono 文件部分
     * @return 上传结果
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
                    throw new HttpClientException(HttpStatus.UNAUTHORIZED, "无法认证或已失效");
                }
                if (user.isExpired()) {
                    throw new HttpClientException(HttpStatus.UNAUTHORIZED, "使用许可已过期");
                }
                return user;
            }).flatMap(user ->
                    fp.transferTo(p)
                            .then(WebHandler.ip())
                            .flatMap(ip -> Mono.just(CommonResponse.success("文件上传成功，请核对文件信息")).doFinally(s -> confirmFile(p.toFile(), user, ip)))
            );
        });
    }

    /**
     * 获取用户
     *
     * @param id 用户id
     * @return 用户信息
     */
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

    /**
     * 验证用户
     *
     * @param sign 签名
     * @param user 用户
     * @return 验证结果
     */
    private Mono<Boolean> valid(String sign, AccessUser user) {
        String group = prepareKey(user.getId(), FILE_UPLOAD_APPLY_GROUP);
        return validService.valid(group, sign, user.getValidKey())
                .flatMap(success -> Boolean.TRUE.equals(success) ?
                        Mono.just(true) : Mono.error(new HttpClientException(HttpStatus.FORBIDDEN, "核验失败，请重新验证"))
                );
    }

    /**
     * 获取验证组名
     *
     * @param id    用户id
     * @param group 权限所属组
     * @return 验证组名
     */
    private String prepareKey(String id, String group) {
        return String.join("_", group, id);
    }

    /**
     * 手动确认是否保存文件
     *
     * @param f    已储存至本地的文件
     * @param user 上传用户
     * @param ip   上传来源
     */
    private void confirmFile(File f, AccessUser user, String ip) {
        String md5;
        try {
            md5 = ConvertUtil.toHex(DigestUtils.digest(DigestUtils.getMd5Digest(), f));
        } catch (IOException e) {
            throw new HttpServerException(HttpStatus.INTERNAL_SERVER_ERROR, "服务器IO异常");
        }

        String msg = String.format("请确认是否保留文件: %s\n" +
                "来自设备: %s\n" +
                "大小: %s\n" +
                "md5摘要: %s", f.getName(), user.getAlias(), ConvertUtil.toShortSize(f.length()), md5);
        if (!WinDropApplication.confirm("来自" + ip, msg)) {
            try {
                Files.delete(Paths.get(f.toURI()));
            } catch (IOException e) {
                WinDropApplication.alert("删除失败，请手动删除，文件路径: " + f.getAbsolutePath());
            }
        } else {
            WinDropApplication.open(f.getParentFile());
        }
    }

}
