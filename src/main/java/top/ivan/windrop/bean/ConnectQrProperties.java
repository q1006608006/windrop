package top.ivan.windrop.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import top.ivan.windrop.random.RandomEncrypt;
import top.ivan.windrop.util.IDUtil;
import top.ivan.windrop.util.JSONUtil;

/**
 * @author Ivan
 * @since 2021/08/13 15:43
 */
public class ConnectQrProperties extends QrProperties {
    @JsonProperty
    private final String token;

    @JsonProperty
    private final String data;

    @Getter
    @JsonIgnore
    private final Option option;

    public ConnectQrProperties(String token, int maxAccess, RandomEncrypt encryptor) {
        this.option = new Option(token, maxAccess, IDUtil.getShortUuid());
        this.token = token;
        this.data = encryptor.encrypt(JSONUtil.toString(this.option));
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Option {
        private String token;
        private int maxAccess;
        private String salt;
    }
}
