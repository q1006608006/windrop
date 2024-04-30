package top.ivan.jdrop.exchange.userinterface.vo.upload;

import lombok.Data;
import lombok.EqualsAndHashCode;
import top.ivan.jdrop.framework.constant.MediaType;
import top.ivan.jdrop.framework.module.ExchangeData;

@EqualsAndHashCode(callSuper = true)
@Data
public class UploadData extends ExchangeData {
    private MediaType mediaType;
    private String resourceName;
    private String content;
}
