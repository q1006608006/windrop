package top.ivan.windrop.svc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Ivan
 * @since 2021/09/06 10:53
 */
@Service
public class LocalQRTextHandler {
    @Autowired
    private QrCodeControllerService qrCodeService;

    public String shareText(String text) {
        return qrCodeService.register(k -> text, 10, 60 * 10);
    }
}
