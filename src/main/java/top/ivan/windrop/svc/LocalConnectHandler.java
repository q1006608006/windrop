package top.ivan.windrop.svc;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.ivan.windrop.bean.WindropConfig;
import top.ivan.windrop.util.SystemUtil;

import java.util.Objects;

import static top.ivan.windrop.WinDropConfiguration.CONNECT_GROUP;

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

    public String newConnect() {
        JSONObject data = new JSONObject();
        data.put("ipList", SystemUtil.getLocalIPList());
        data.put("token", keyService.getKey(CONNECT_GROUP));
        data.put("port", config.getPort());
        String qrBody = data.toJSONString();
        return qrCodeService.register(k -> qrBody, 1, 60);
    }

    public boolean match(String deviceId, String sign) {
        return keyService.match(CONNECT_GROUP, key -> Objects.equals(DigestUtils.sha256Hex(String.join(";", deviceId, key, config.getPassword())), sign));
    }

}
