package top.ivan.windrop.svc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.ivan.windrop.bean.ConnectQrProperties;
import top.ivan.windrop.bean.WindropConfig;
import top.ivan.windrop.exception.BadEncryptException;
import top.ivan.windrop.random.RandomEncrypt;
import top.ivan.windrop.util.JSONUtils;

import static top.ivan.windrop.WinDropConfiguration.CONNECT_GROUP;

/**
 * @author Ivan
 * @description
 * @date 2021/3/31
 */
@Service
public class LocalQRConnectHandler extends LocalQRHandler {

    private static final int EXPIRE_TIME = 90;

    @Autowired
    private QrCodeControllerService qrCodeService;

    @Autowired
    private ValidService validService;

    private final RandomEncrypt randomEncrypt = new RandomEncrypt(EXPIRE_TIME);

    public LocalQRConnectHandler(WindropConfig config) {
        super(config);
    }

    public String newConnect(int second) {
        String token = validService.getValidKey(CONNECT_GROUP, EXPIRE_TIME);
        ConnectQrProperties qrData = new ConnectQrProperties(token, second, randomEncrypt);
        fixHost("connect", qrData);

        String qrBody = JSONUtils.toString(qrData);
        return qrCodeService.register(k -> qrBody, 1, EXPIRE_TIME);
    }

    public ConnectQrProperties.Option getOption(String data) throws BadEncryptException {
        String body = randomEncrypt.decrypt(data);
        return JSONUtils.read(body, ConnectQrProperties.Option.class);
    }

    public void reset() {
        randomEncrypt.expired();
    }

}
