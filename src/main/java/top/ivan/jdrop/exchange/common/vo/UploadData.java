package top.ivan.jdrop.exchange.common.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import top.ivan.jdrop.common.constant.MediaType;

@EqualsAndHashCode(callSuper = true)
@Data
public class UploadData extends ExchangeData {
    private MediaType mediaType;
    private String resourceName;
    private String content;
}
