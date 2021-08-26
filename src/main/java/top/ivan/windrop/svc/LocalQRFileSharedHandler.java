package top.ivan.windrop.svc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.ivan.windrop.bean.FileQrProperties;
import top.ivan.windrop.bean.WindropConfig;
import top.ivan.windrop.util.IDUtil;
import top.ivan.windrop.util.JSONUtil;

import java.io.File;

/**
 * @author Ivan
 * @since 2021/04/27 11:20
 */
@Slf4j
@Service
public class LocalQRFileSharedHandler extends LocalQRHandler {

    @Autowired
    private ResourceSharedService sharedService;

    @Autowired
    private QrCodeControllerService qrCodeService;

    public LocalQRFileSharedHandler(WindropConfig config) {
        super(config);
    }

    public String sharedFile(File file, int count, int second) {
        return qrCodeService.register(key -> {
            String sharedKey = IDUtil.getShortUuid();
            sharedService.register(sharedKey, file, 1, second);

            FileQrProperties props = new FileQrProperties(sharedKey);
            fixHost("file", props);

            log.info("share file[{}] with key '{}'", file.getName(), sharedKey);
            return JSONUtil.toString(props);
        }, count, second);
    }

}
