package top.ivan.windrop.svc;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.ivan.windrop.bean.WindropConfig;
import top.ivan.windrop.ex.BadEncryptException;
import top.ivan.windrop.random.RandomEncrypt;
import top.ivan.windrop.util.IDUtil;

import java.util.Objects;

import static top.ivan.windrop.WinDropConfiguration.CONNECT_GROUP;

/**
 * @author Ivan
 * @description
 * @date 2021/3/31
 */
@Service
public class LocalQRConnectHandler extends LocalQRHandler {

    @Autowired
    private QrCodeControllerService qrCodeService;
    @Autowired
    private RandomAccessKeyService keyService;

    private final RandomEncrypt randomEncrypt = new RandomEncrypt(90);

    public LocalQRConnectHandler(WindropConfig config) {
        super(config);
    }

    public String newConnect(int second) {
        JSONObject qrData = baseRequest("connect");

        String token = keyService.getKey(CONNECT_GROUP);
        qrData.put("token", token);
        JSONObject option = new JSONObject();
        option.put("maxAccess", second);
        option.put("token", token);
        option.put("salt", IDUtil.getShortUuid());
        String encryptData = randomEncrypt.encrypt(option.toJSONString());
        qrData.put("data", encryptData);

        String qrBody = qrData.toJSONString();
        return qrCodeService.register(k -> qrBody, 1, 90);
    }

    public boolean match(String locate, String deviceId, String sign) {
        return keyService.match(CONNECT_GROUP, key -> Objects.equals(DigestUtils.sha256Hex(String.join(";", locate, deviceId, key, config.getPassword())), sign));
    }

    public JSONObject getOption(String data) throws BadEncryptException {
        String body = randomEncrypt.decrypt(data);
        return JSONObject.parseObject(body);
    }

    public void reset() {
        randomEncrypt.expired();
    }

}
