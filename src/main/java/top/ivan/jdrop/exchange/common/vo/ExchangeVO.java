package top.ivan.jdrop.exchange.common.vo;

import lombok.Data;
import top.ivan.jdrop.common.constant.MediaType;

@Data
public class ExchangeVO<T extends ExchangeData> {
    private String id;
    private String token;
    private MediaType mediaType;
    private T data;
}
