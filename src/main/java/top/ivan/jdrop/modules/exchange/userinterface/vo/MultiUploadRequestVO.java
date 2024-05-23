package top.ivan.jdrop.modules.exchange.userinterface.vo;

import lombok.Data;
import top.ivan.jdrop.framework.constant.MediaType;

import java.util.List;

@Data
public class MultiUploadRequestVO {
    private String deviceId;
    private MediaType mediaType;
    private List<String> peekList;
}
