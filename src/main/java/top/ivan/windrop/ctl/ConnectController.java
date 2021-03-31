package top.ivan.windrop.ctl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import top.ivan.windrop.WinDropApplication;
import top.ivan.windrop.bean.AccessUser;
import top.ivan.windrop.svc.LocalConnectHandler;
import top.ivan.windrop.svc.PersistUserService;
import top.ivan.windrop.util.IDUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

    @PostMapping("connect")
    public ResponseEntity<Map<String, Object>> connect(@RequestBody Map<String, String> request, ServerWebExchange exchange) {
        log.info("receive decrypt request from '{}'", exchange.getRequest().getRemoteAddress());

        String sign = request.getOrDefault("sign", "");
        String deviceId = request.getOrDefault("deviceId", "");

        if (!handler.match(deviceId, sign)) {
            log.info("valid failed, reject it");
            return failure(HttpStatus.UNAUTHORIZED, "未通过核验");
        }

        String uid = IDUtil.getShortUuid();
        AccessUser user;
        try {
            boolean confirmNewUser = WinDropApplication.WindropHandler.confirm("新连接", "是否允许" + deviceId + "(" + exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() + ")连接windrop?");
            if (!confirmNewUser) {
                return failure(HttpStatus.OK, "拒绝连接");
            }
            user = userService.newUser(uid, deviceId, IDUtil.get32UUID());
        } catch (IOException e) {
            throw new RuntimeException("数据文件无法访问", e);
        }

        Map<String, Object> data = new HashMap<>();
        log.info("valid success");
        data.put("msg", "");
        data.put("success", true);
        data.put("id", uid);
        data.put("validKey", user.getValidKey());
        return ResponseEntity.ok(data);
    }

    private ResponseEntity<Map<String, Object>> failure(HttpStatus status, String msg) {
        Map<String, Object> data = new HashMap<>();
        data.put("success", false);
        data.put("message", msg);
        return ResponseEntity.status(status).body(data);
    }

}
