package top.ivan.jdrop.modules.device.stable.module.dto;

import lombok.Data;

/**
 * @author Ivan
 * @since 2024/03/15 09:42
 */
@Data
public class DeviceConnectDTO {
    private String keyRoot;
    private String tokenElement;
    private String hostName;
    private int netPort;
}
