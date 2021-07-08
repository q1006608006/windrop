package top.ivan.windrop.svc;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import top.ivan.windrop.bean.WindropConfig;
import top.ivan.windrop.util.SystemUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ivan
 * @since 2021/07/06 17:33
 */
public abstract class LocalQRHandler {
    @Getter
    protected final WindropConfig config;

    public LocalQRHandler(WindropConfig config) {
        this.config = config;
    }

    public JSONObject baseRequest(String type) {
        JSONObject qrData = new JSONObject();
        qrData.put("type", type);
        List<String> ipList = new ArrayList<>(SystemUtil.getLocalIPList());
        ipList.remove("127.0.0.1");
        qrData.put("ipList", ipList);
        qrData.put("port", config.getPort());

        return qrData;
    }
}
