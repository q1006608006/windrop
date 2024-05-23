package top.ivan.jdrop.modules.device.stable.module.vo;

import lombok.Data;
import top.ivan.jdrop.framework.constant.MediaType;
import top.ivan.jdrop.framework.constant.Operate;

@Data
public class AccessInfoVO {
    private final String deviceId;
    private final Operate operate;
    private final MediaType mediaType;
    private final String digest;
}
