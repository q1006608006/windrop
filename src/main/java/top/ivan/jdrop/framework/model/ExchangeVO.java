package top.ivan.jdrop.framework.model;

import lombok.Data;
import top.ivan.jdrop.framework.constant.MediaType;

@Data
public class ExchangeVO<T extends ExchangeData> {
    private String id;
    private String token;
    private MediaType mediaType;
    private T data;
}
