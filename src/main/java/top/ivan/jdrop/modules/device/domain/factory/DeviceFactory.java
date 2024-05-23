package top.ivan.jdrop.modules.device.domain.factory;

import org.springframework.stereotype.Component;
import top.ivan.jdrop.modules.device.domain.entity.DeviceEntity;
import top.ivan.jdrop.modules.device.domain.entity.ConnectProtocolEntity;

/**
 * @author Ivan
 * @since 2024/03/20 17:22
 */
@Component
public class DeviceFactory {
    public DeviceEntity newDevice(String id, String name, ConnectProtocolEntity protocol) {
        DeviceEntity device = new DeviceEntity();
        device.setId(id);
        device.setAlias(name);
        device.setValidKey(protocol.getValidKey());
        device.setExpireTime(protocol.nextExpireTime());
        device.setAccessTime(System.currentTimeMillis());
        return device;
    }
}
