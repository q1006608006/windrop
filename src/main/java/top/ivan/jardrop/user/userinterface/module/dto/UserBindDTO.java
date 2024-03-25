package top.ivan.jardrop.user.userinterface.module.dto;

import lombok.Data;

/**
 * @author Ivan
 * @since 2024/03/21 13:49
 */
@Data
public class UserBindDTO {
    private final String id;
    private final String keyElement;

    public UserBindDTO(String id, String keyElement) {
        this.keyElement = keyElement;
        this.id = id;
    }
}
