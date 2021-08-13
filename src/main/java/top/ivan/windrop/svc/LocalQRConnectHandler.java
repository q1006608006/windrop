package top.ivan.windrop.svc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.ivan.windrop.bean.ConnectQrProperties;
import top.ivan.windrop.bean.WindropConfig;
import top.ivan.windrop.ex.BadEncryptException;
import top.ivan.windrop.random.RandomEncrypt;
import top.ivan.windrop.util.JSONUtil;

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
    private ValidService validService;

    private final RandomEncrypt randomEncrypt = new RandomEncrypt(90);

    public LocalQRConnectHandler(WindropConfig config) {
        super(config);
    }

    public String newConnect(int second) {
        String token = validService.getValidKey(CONNECT_GROUP, 90);
        ConnectQrProperties qrData = new ConnectQrProperties(token, second, randomEncrypt);
        fixHost("connect", qrData);

        String qrBody = JSONUtil.toString(qrData);
        return qrCodeService.register(k -> qrBody, 1, 90);
    }

    public ConnectQrProperties.Option getOption(String data) throws BadEncryptException {
        String body = randomEncrypt.decrypt(data);
        return JSONUtil.read(body, ConnectQrProperties.Option.class);
    }

    public void reset() {
        randomEncrypt.expired();
    }

}
