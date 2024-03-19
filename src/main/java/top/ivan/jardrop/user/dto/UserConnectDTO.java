package top.ivan.jardrop.user.dto;

import lombok.Data;

/**
 * @author Ivan
 * @since 2024/03/15 09:42
 */
@Data
public class UserConnectDTO {
    private String keyRoot;
    private String tokenElement;
    private String hostName;
}
