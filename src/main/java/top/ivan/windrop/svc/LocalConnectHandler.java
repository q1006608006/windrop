package top.ivan.windrop.svc;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.ivan.windrop.bean.WindropConfig;
import top.ivan.windrop.util.ConvertUtil;
import top.ivan.windrop.util.IDUtil;
import top.ivan.windrop.util.SystemUtil;

import java.util.Objects;

import static top.ivan.windrop.WinDropConfiguration.CONNECT_GROUP;
import static top.ivan.windrop.WinDropConfiguration.RANDOM_SECURITY_KEY_GROUP;

/**
 * @author Ivan
 * @description
 * @date 2021/3/31
 */
@Service
public class LocalConnectHandler {

    @Autowired
    private QrCodeControllerService qrCodeService;
    @Autowired
    private WindropConfig config;
    @Autowired
    private RandomAccessKeyService keyService;

    public String newConnect(int second) {
        JSONObject qrData = new JSONObject();
        qrData.put("type", "connect");
        qrData.put("ipList", SystemUtil.getLocalIPList());
        qrData.put("token", keyService.getKey(CONNECT_GROUP));
        qrData.put("port", config.getPort());
        JSONObject option = new JSONObject();
        option.put("maxAccess", second);
        option.put("salt", IDUtil.getShortUuid());
        String encryptData = ConvertUtil.encrypt(option.toJSONString(), keyService.getKey(RANDOM_SECURITY_KEY_GROUP, 90));
        qrData.put("data", encryptData);

        String qrBody = qrData.toJSONString();
        return qrCodeService.register(k -> qrBody, 1, 60);
    }

    public boolean match(String locate, String deviceId, String sign) {
        return keyService.match(CONNECT_GROUP, key -> Objects.equals(DigestUtils.sha256Hex(String.join(";", locate, deviceId, key, config.getPassword())), sign));
    }

    public JSONObject getOption(String data) {
        String body = ConvertUtil.decrypt(data, keyService.getKey(RANDOM_SECURITY_KEY_GROUP, 90));
        return JSONObject.parseObject(body);
    }

}
