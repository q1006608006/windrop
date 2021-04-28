package top.ivan.windrop.svc;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.ivan.windrop.util.IDUtil;
import top.ivan.windrop.util.SystemUtil;

import java.io.File;

/**
 * @author Ivan
 * @since 2021/04/27 11:20
 */
@Slf4j
@Service
public class LocalQRFileSharedHandler {

    @Autowired
    private ResourceSharedService sharedService;

    @Autowired
    private QrCodeControllerService qrCodeService;

    public String sharedFile(File file, int count, int second) {
        String sharedKey = IDUtil.getShortUuid();
        sharedService.register(sharedKey, file, count, second);
        JSONObject obj = new JSONObject();
        obj.put("type", "file");
        obj.put("code", sharedKey);
        obj.put("ipList", SystemUtil.getLocalIPList());
        String qrCodeBody = obj.toJSONString();
        log.info("share file[{}] with key '{}'", file.getName(), sharedKey);
        return qrCodeService.register(key -> qrCodeBody, count, second);
    }

}
