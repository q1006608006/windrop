package top.ivan.windrop.ctl;

import com.alibaba.fastjson.JSONObject;
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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import top.ivan.windrop.WinDropConfiguration;
import top.ivan.windrop.bean.AccessUser;
import top.ivan.windrop.bean.ApplyResponse;
import top.ivan.windrop.bean.CommonResponse;
import top.ivan.windrop.ex.HttpClientException;
import top.ivan.windrop.ex.HttpServerException;
import top.ivan.windrop.random.RandomEncrypt;
import top.ivan.windrop.svc.PersistUserService;
import top.ivan.windrop.svc.RandomAccessKeyService;
import top.ivan.windrop.svc.ResourceSharedService;
import top.ivan.windrop.util.IDUtil;
import top.ivan.windrop.verify.VerifyIP;

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
    public static final String FILE_UPLOAD_APPLY_GROUP = "FILE_UPLOAD_APPLY";
    public static final String FILE_UPLOAD_GROUP = "FILE_UPLOAD";

    @Autowired
    private ResourceSharedService resourceSharedService;
    @Autowired
    private RandomAccessKeyService keyService;
    @Autowired
    private PersistUserService userService;

    private final RandomEncrypt randomEncrypt = new RandomEncrypt(240);

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
     * 获取上传请求accessKey，周期内幂等，使用POST请求
     *
     * @return {@link ApplyResponse}
     */
    @ResponseBody
    @PostMapping("upload/apply")
    @VerifyIP
    public Mono<ApplyResponse> uploadApply(@RequestParam String id) {
        return Mono.just(ApplyResponse.success(keyService.getKey(prepareKey(prepareUser(id), FILE_UPLOAD_APPLY_GROUP), 30)));
    }

    //获取applyKey
    //客户端获取applyKey+自身validKey进行sha256请求page页面
    //

    @GetMapping("upload/{id}")
    @VerifyIP
    public Mono<String> uploadModel(@RequestParam String key, @PathVariable String id, Mono<Model> mono) {
        AccessUser user = prepareUser(id);
        validApply(key, user);

        String matchKey = keyService.getKey(prepareKey(user, FILE_UPLOAD_GROUP), 3 * 60);
        JSONObject obj = new JSONObject();
        obj.put("userId", user.getId());
        obj.put("key", matchKey);
        obj.put("salt", System.currentTimeMillis());

        return mono.doOnNext(model -> model.addAttribute("hidden", randomEncrypt.encrypt(obj.toString()))).thenReturn("upload");
    }

    @PostMapping("upload/confirm")
    @VerifyIP
    public Mono<CommonResponse> uploadConfirm() {
        return null;
    }

    @ResponseBody
    @PostMapping("upload")
    @VerifyIP
    public Mono<CommonResponse> fileUpload(@RequestPart("file") Mono<FilePart> mono, @RequestPart("hidden") String hidden) {
        return mono.doFirst(() -> {
            //todo
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
        if (!keyService.match(prepareKey(user, FILE_UPLOAD_APPLY_GROUP), k -> DigestUtils.sha256Hex(user.getValidKey() + ";" + k).equals(key))) {
            throw new HttpClientException(HttpStatus.FORBIDDEN, "核验未通过");
        }
    }

    private String prepareKey(AccessUser user, String group) {
        return String.join("_", group, user.getId());
    }

}
