package top.ivan.jdrop.modules.exchange.stable.vo;

import lombok.Data;
import top.ivan.jdrop.framework.constant.MediaType;

import java.util.List;

/**
 * @author Ivan
 * @since 2024/03/29 00:24
 */
@Data
public class UploadInfoVO {
    private String deviceId;
    private MediaType mediaType;
    private List<String> digests;
}
