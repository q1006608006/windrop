package top.ivan.windrop.bean;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * @author Ivan
 * @description
 * @date 2021/2/19
 */
@Data
@Component
@ConfigurationProperties(prefix = "windrop")
@PropertySource("classpath:config.properties")
public class WindropConfig {

    private int port;
    private long maxFileLength;
    private long textFileLimit;
    private String encoding;
    private List<String> networkInterfaces;
    private String shortcutApi = "shortcut.json";

    public boolean needNotify(String type, boolean isPush) {
        return true; //todo
    }

    public boolean needConfirm(String type, boolean push) {
        return true; //todo
    }


    public Charset getCharset() {
        return StringUtils.hasLength(encoding) ? Charset.forName(encoding) : StandardCharsets.UTF_8;
    }

}
