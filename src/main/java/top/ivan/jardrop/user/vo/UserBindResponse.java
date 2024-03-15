package top.ivan.jardrop.user.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Ivan
 * @since 2024/03/11 10:16
 */
@Data
@AllArgsConstructor
public class UserBindResponse {
    private String id;
    private String receipt;
}
