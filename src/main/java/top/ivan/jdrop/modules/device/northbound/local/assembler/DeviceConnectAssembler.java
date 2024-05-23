package top.ivan.jdrop.modules.device.northbound.local.assembler;

import top.ivan.jdrop.modules.device.stable.module.dto.DeviceConnectDTO;
import top.ivan.jdrop.modules.device.domain.entity.ConnectProtocolEntity;

/**
 * @author Ivan
 * @since 2024/03/11 11:11
 */
public class DeviceConnectAssembler {
    public DeviceConnectDTO convertToDeviceConnect(ConnectProtocolEntity p, String hostname, int port) {
        DeviceConnectDTO dto = new DeviceConnectDTO();
        dto.setKeyRoot(p.getKeyRoot());
        dto.setTokenElement(p.getTokenElement());
        dto.setHostName(hostname);
        dto.setNetPort(port);
        return dto;
    }
}
