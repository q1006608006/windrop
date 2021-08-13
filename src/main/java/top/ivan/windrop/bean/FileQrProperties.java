package top.ivan.windrop.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * @author Ivan
 * @since 2021/08/13 16:04
 */
public class FileQrProperties extends QrProperties {
    @Getter
    private final String code;

    public FileQrProperties(String code) {
        this.code = code;
    }
}
