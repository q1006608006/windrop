package top.ivan.jdrop.exchange.userinterface.vo.security;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Ivan
 * @since 2024/03/28 17:21
 */
@Data
@AllArgsConstructor
public class SignVO {
    private String sign;
    private String token;
}
