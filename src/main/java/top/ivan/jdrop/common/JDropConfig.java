package top.ivan.jdrop.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author Ivan
 * @description
 * @date 2021/2/19
 */
@Data
@Component
@ConfigurationProperties(prefix = "jdrop")
@PropertySource("classpath:jdrop.properties")
public class JDropConfig {

    private int port;
    private String fileEncoding;
    private List<String> networkInterfaces;
    private String shortcutApi = "shortcut.json";

    public boolean needNotify(String type, boolean isPush) {
        return true; //todo
    }

    public boolean needConfirm(String type, boolean push) {
        return true; //todo
    }


    public Charset getCharset() {
        return StringUtils.hasLength(fileEncoding) ? Charset.forName(fileEncoding) : StandardCharsets.UTF_8;
    }

}
