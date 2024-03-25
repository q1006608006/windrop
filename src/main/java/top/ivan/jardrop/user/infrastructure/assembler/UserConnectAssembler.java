package top.ivan.jardrop.user.infrastructure.assembler;

import top.ivan.jardrop.common.util.NetUtils;
import top.ivan.jardrop.user.domain.ConnectProtocolEntity;
import top.ivan.jardrop.user.userinterface.module.dto.UserConnectDTO;

/**
 * @author Ivan
 * @since 2024/03/11 11:11
 */
public class UserConnectAssembler {
    public UserConnectDTO convertToUserConnect(ConnectProtocolEntity p) {
        UserConnectDTO dto = new UserConnectDTO();
        dto.setKeyRoot(p.getKeyRoot());
        dto.setTokenElement(p.getTokenElement());
        dto.setHostName(NetUtils.getLocalHostName());
        return dto;
    }
}
