package top.ivan.windrop.ctl;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import top.ivan.windrop.WinDropApplication;
import top.ivan.windrop.WinDropConfiguration;
import top.ivan.windrop.bean.AccessUser;
import top.ivan.windrop.bean.ConnectRequest;
import top.ivan.windrop.bean.ConnectResponse;
import top.ivan.windrop.bean.WindropConfig;
import top.ivan.windrop.ex.HttpClientException;
import top.ivan.windrop.ex.HttpServerException;
import top.ivan.windrop.svc.LocalQRConnectHandler;
import top.ivan.windrop.svc.PersistUserService;
import top.ivan.windrop.svc.ValidService;
import top.ivan.windrop.util.ConvertUtil;
import top.ivan.windrop.util.IDUtil;
import top.ivan.windrop.util.SystemUtil;
import top.ivan.windrop.verify.WebHandler;

import java.io.IOException;

/**
 * @author Ivan
 * @description 设备连接windrop的控制器
 * @date 2021/2/5
 */
@Slf4j
@RestController
@RequestMapping("/windrop/auth")
public class ConnectController {

    /**
     * 用户（可连接设备）服务
     */
    @Autowired
    private PersistUserService userService;

    /**
     * 与本控制器共享数据的handler
     */
    @Autowired
    private LocalQRConnectHandler connectHandler;

    @Autowired
    private WindropConfig config;

    @Autowired
    private ValidService validService;

    /**
     * 与windrop创建连接或更新连接
     *
     * @param mono 连接请求
     * @return 连接windrop的id和validKey
     */
    @PostMapping("connect")
    public Mono<ConnectResponse> connect(@RequestBody Mono<ConnectRequest> mono) {
        return valid(mono).flatMap(request ->
                WebHandler.request()
                        .map(req -> req.getRemoteAddress().getAddress().getHostAddress())
                        .flatMap(host -> {
                            // 连接参数
                            JSONObject option = connectHandler.getOption(request.getData());
                            // 弹窗确认是否同意连接
                            if (!confirm(option, request, host)) {
                                return Mono.just(failure("拒绝连接"));
                            } else {
                                connectHandler.reset();
                                return createUser(option, request);
                            }
                        })
        );
    }

    private Mono<ConnectRequest> valid(Mono<ConnectRequest> mono) {
        return mono.flatMap(req -> {
            // 校验参数
            if (StringUtils.isEmpty(req.getDeviceId())
                    || StringUtils.isEmpty(req.getSign())
                    || StringUtils.isEmpty(req.getData())) {
                return Mono.error(new HttpClientException(HttpStatus.BAD_REQUEST, "bad request"));
            }
            // 验证设备有效性(sha256(wifi名称,设备ID,randomKey,核验密钥))
            return validService.valid(WinDropConfiguration.CONNECT_GROUP, req.getSign(), req.getLocate(), req.getDeviceId()).flatMap(success -> {
                if (!success) {
                    return Mono.error(new HttpClientException(HttpStatus.FORBIDDEN, "核验失败，认证码过期或已被使用"));
                }
                return Mono.just(req);
            });
        });
    }

    private Mono<ConnectResponse> createUser(JSONObject option, ConnectRequest request) {
        // 连接有效期
        Integer maxAccess = option.getInteger("maxAccess");
        String token = option.getString("token");

        // 生成设备ID及验证密钥
        String uid = generateId(request.getDeviceId(), request.getLocate());
        String validKey = IDUtil.get32UUID();
        String realPwd = DigestUtils.sha256Hex(ConvertUtil.combines(";", validKey, token));
        return Mono.fromSupplier(() -> {
            try {
                return userService.newUser(uid, request.getDeviceId(), realPwd, maxAccess);
            } catch (IOException e) {
                log.error("create new user failed", e);
                return Mono.error(new HttpServerException(HttpStatus.INTERNAL_SERVER_ERROR, "数据服务异常"));
            } catch (Exception e) {
                log.error("init user failed with option: " + option, e);
                return Mono.error(new HttpClientException(HttpStatus.BAD_REQUEST, "bad request"));
            }
        }).cast(AccessUser.class).map(user -> {
            log.info("accept new connector for {}[{}]", user.getAlias(), user.getId());
            return ok(uid, validKey);
        });
    }

    private boolean confirm(JSONObject opt, ConnectRequest request, String host) {
        Integer maxAccess = opt.getInteger("maxAccess");
        String accessTime;
        if (maxAccess < 0) {
            accessTime = "永久";
        } else if (maxAccess % 60 != 0 || maxAccess < 60) {
            accessTime = maxAccess + "秒";
        } else if (maxAccess % 3600 != 0 || maxAccess < 3600) {
            accessTime = maxAccess / 60 + "分钟";
        } else if (maxAccess % 3600 * 24 != 0 || maxAccess < 3600 * 24) {
            accessTime = maxAccess / 3600 + "小时";
        } else {
            accessTime = maxAccess / (3600 * 24) + "天";
        }
        return WinDropApplication.WindropHandler.confirm("新连接", "是否允许" + request.getDeviceId() + "(" + host + ")连接windrop[" + accessTime + "]?");
    }

    private ConnectResponse failure(String msg) {
        ConnectResponse rsp = new ConnectResponse();
        rsp.setSuccess(false);
        rsp.setMessage(msg);
        return rsp;
    }

    private ConnectResponse ok(String id, String key) {
        ConnectResponse rsp = new ConnectResponse();
        rsp.setSuccess(true);
        rsp.setId(id);
        rsp.setValidKey(key);
        return rsp;
    }

    /**
     * 生成用户id（非随机）
     *
     * @param deviceId 设备名称
     * @param locate   一般为wifi名称
     * @return 用户ID
     */
    private String generateId(String deviceId, String locate) {
        return DigestUtils.sha256Hex(SystemUtil.getSystemKey() + ";" + deviceId + ";" + locate).substring(0, 8);
    }
}
