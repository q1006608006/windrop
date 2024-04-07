package top.ivan.jdrop.user.infrastructure.assembler;

import top.ivan.jdrop.user.domain.model.ConnectProtocolEntity;
import top.ivan.jdrop.user.userinterface.model.dto.UserConnectDTO;

/**
 * @author Ivan
 * @since 2024/03/11 11:11
 */
public class UserConnectAssembler {
    public UserConnectDTO convertToUserConnect(ConnectProtocolEntity p, String hostname, int port) {
        UserConnectDTO dto = new UserConnectDTO();
        dto.setKeyRoot(p.getKeyRoot());
        dto.setTokenElement(p.getTokenElement());
        dto.setHostName(hostname);
        dto.setNetPort(port);
        return dto;
    }
}
