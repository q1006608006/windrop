package top.ivan.jdrop.modules.device.southbound.ports;


import io.vavr.control.Option;
import top.ivan.jdrop.modules.device.domain.entity.DeviceEntity;

/**
 * @author Ivan
 * @since 2023/06/30 13:48
 */
public interface DeviceRepository {

    void saveDevice(DeviceEntity user);

    Option<DeviceEntity> findDevice(String id);


}
