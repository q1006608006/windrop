package top.ivan.jdrop.modules.device.stable.module.dto;

import lombok.Data;

/**
 * @author Ivan
 * @since 2024/03/21 13:49
 */
@Data
public class DeviceBindDTO {
    private final String id;
    private final String keyElement;

    public DeviceBindDTO(String id, String keyElement) {
        this.keyElement = keyElement;
        this.id = id;
    }
}
