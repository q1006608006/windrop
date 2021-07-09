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
    private static final List<String> operatorList = Arrays.asList("pull", "text", "file", "image");

    private int port;
    private String username;
    private String password;
    private long maxFileLength;
    private String encoding;
    private boolean enableShowText;
    private List<String> notify;
    private List<String> confirm;
    private long fileExpiredSecond;

    public boolean needNotify(String type, boolean isPush) {
        if (null == notify) {
            return false;
        }
        String iType = type.toLowerCase();
        return notify.stream().map(String::toLowerCase).anyMatch(s -> s.equals("*") || s.equals(iType) || s.equals(isPush ? "push.*" : "pull.*") || s.equals((isPush ? "push." : "pull.") + iType));
    }

    public boolean needConfirm(String type, boolean push) {
        String iType = type.toLowerCase();
        return null == confirm
                || confirm.stream().map(String::toLowerCase).anyMatch(s -> s.equals("*")
                || s.equals(iType)
                || s.equals(push ? "push.*" : "pull.*")
                || s.equals((push ? "push." : "pull.") + iType))
                || !operatorList.contains(iType);
    }

    public Charset getCharset() {
        return StringUtils.isEmpty(encoding) ? StandardCharsets.UTF_8 : Charset.forName(encoding);
    }

}
