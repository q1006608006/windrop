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
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import top.ivan.windrop.WinDropApplication;
import top.ivan.windrop.bean.AccessUser;
import top.ivan.windrop.bean.ConnectRequest;
import top.ivan.windrop.bean.ConnectResponse;
import top.ivan.windrop.ex.HttpClientException;
import top.ivan.windrop.ex.HttpServerException;
import top.ivan.windrop.svc.LocalConnectHandler;
import top.ivan.windrop.svc.PersistUserService;
import top.ivan.windrop.util.IDUtil;
import top.ivan.windrop.util.SystemUtil;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * @author Ivan
 * @description
 * @date 2021/2/5
 */
@Slf4j
@RestController
@RequestMapping("/windrop/auth")
public class ConnectController {

    @Autowired
    private PersistUserService userService;
    @Autowired
    private LocalConnectHandler handler;

    /**
     * 与windrop创建连接
     * @param mono 连接请求
     * @param exchange web请求信息
     * @return 连接windrop的id和validKey
     */
    @PostMapping("connect")
    public Mono<ConnectResponse> connect(@RequestBody Mono<ConnectRequest> mono, ServerWebExchange exchange) {
        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
        log.info("receive decrypt request from '{}'", remoteAddress);

        return mono.doOnNext(request -> {
            if (StringUtils.isEmpty(request.getDeviceId())
                    || StringUtils.isEmpty(request.getSign())
                    || StringUtils.isEmpty(request.getData())) {
                throw new HttpClientException(HttpStatus.BAD_REQUEST, "bad request");
            }
            if (!handler.match(request.getLocate(), request.getDeviceId(), request.getSign())) {
                log.info("valid failed, reject it");
                throw new HttpClientException(HttpStatus.UNAUTHORIZED, "未通过核验");
            }
        }).map(request -> {
            JSONObject option;
            Integer maxAccess;
            try {
                option = handler.getOption(request.getData());
                maxAccess = option.getInteger("maxAccess");
            } catch (Exception e) {
                throw new HttpClientException(HttpStatus.BAD_REQUEST, "数据无效或过期");
            }
            if (!confirm(maxAccess, request, remoteAddress.getAddress().getHostAddress())) {
                return failure("拒绝连接");
            }
            String uid = generateId(request.getDeviceId(), request.getLocate());
            String validKey = IDUtil.get32UUID();
            try {
                AccessUser user = userService.newUser(uid, request.getDeviceId(), validKey, maxAccess);
                log.info("accept new connector for {}[{}]", user.getAlias(), user.getId());
            } catch (IOException e) {
                log.error("create new user failed", e);
                throw new HttpServerException(HttpStatus.INTERNAL_SERVER_ERROR, "数据服务异常");
            } catch (Exception e) {
                log.error("init user failed with option: " + option, e);
                throw new HttpClientException(HttpStatus.BAD_REQUEST, "bad request");
            }
            return ok(uid, validKey);
        });
    }

    private boolean confirm(int maxAccess, ConnectRequest request, String host) {
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

    private String generateId(String deviceId, String locate) {
        return DigestUtils.sha256Hex(SystemUtil.getSystemKey() + ";" + deviceId + ";" + locate).substring(0, 8);
    }
}
