package top.ivan.windrop.ctl;

import com.alibaba.fastjson.JSONObject;
import com.google.zxing.WriterException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import top.ivan.windrop.WinDropApplication;
import top.ivan.windrop.bean.AccessUser;
import top.ivan.windrop.bean.WindropConfig;
import top.ivan.windrop.svc.PersistUserService;
import top.ivan.windrop.svc.ValidKeyService;
import top.ivan.windrop.util.ConvertUtil;
import top.ivan.windrop.util.IDUtil;
import top.ivan.windrop.util.RandomAccessKey;
import top.ivan.windrop.util.SystemUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Ivan
 * @description
 * @date 2021/2/5
 */
@Slf4j
@RestController
@RequestMapping("/windrop/auth")
public class ConnectController {

    // 二维码的宽度
    static final int WIDTH = 300;
    // 二维码的高度
    static final int HEIGHT = 300;
    // 二维码的格式
    static final String FORMAT = "png";

    @Autowired
    private WindropConfig config;
    @Autowired
    private ValidKeyService keyService;
    @Autowired
    private PersistUserService userService;

    @Autowired
    private ReactiveUserDetailsService ignoreService;

    private final RandomAccessKey tokenKey = new RandomAccessKey(30);

    @GetMapping(value = "connect_code", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQrCode() throws WriterException, IOException {
        return ResponseEntity.ok().body(getCodeBytes());
    }

    @PostMapping("connect")
    public ResponseEntity<Map<String, Object>> connect(@RequestBody Map<String, String> request, ServerWebExchange exchange) {
        log.info("receive decrypt request from '{}'", exchange.getRequest().getRemoteAddress());

        String sign = request.getOrDefault("sign", "");
        String deviceId = request.getOrDefault("deviceId", "");

        if (!tokenKey.match(key -> Objects.equals(DigestUtils.sha256Hex(String.join(";", deviceId, key, config.getPassword())), sign), true)) {
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

    private byte[] getCodeBytes() throws IOException, WriterException {
        JSONObject data = new JSONObject();
        data.put("ipList", SystemUtil.getLocalIPList());
        data.put("token", tokenKey.getAccessKey());
        data.put("validKey", ConvertUtil.encrypt(keyService.getValidKey(), config.getPassword()));
        data.put("port", config.getPort());
        return ConvertUtil.getQrCodeImageBytes(data.toJSONString(), WIDTH, HEIGHT, FORMAT);
    }

    private ResponseEntity<Map<String, Object>> failure(HttpStatus status, String msg) {
        Map<String, Object> data = new HashMap<>();
        data.put("success", false);
        data.put("message", msg);
        return ResponseEntity.status(status).body(data);
    }

}
