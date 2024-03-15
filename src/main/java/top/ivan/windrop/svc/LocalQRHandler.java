package top.ivan.windrop.svc;

import lombok.Getter;
import top.ivan.windrop.bean.QrProperties;
import top.ivan.windrop.bean.WindropConfig;
import top.ivan.windrop.system.SystemUtils;

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

    public <T extends QrProperties> void fixHost(String type, T props) {
        props.setType(type);
        List<String> ipList = new ArrayList<>(SystemUtils.getLocalIPList());
        ipList.remove("127.0.0.1");
        props.setIpList(ipList);
        props.setPort(config.getPort());
    }

    public static String getUrlPath(String host, String key) {
        return host + "/windrop/code/" + key;
    }
}
