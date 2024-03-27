package top.ivan.jdrop.user.infrastructure.factory;

import org.springframework.stereotype.Component;
import top.ivan.jdrop.user.domain.AccessUserEntity;
import top.ivan.jdrop.user.domain.ConnectProtocolEntity;

/**
 * @author Ivan
 * @since 2024/03/20 17:22
 */
@Component
public class UserFactory {
    public AccessUserEntity newUser(String id, String name, ConnectProtocolEntity protocol) {
        AccessUserEntity user = new AccessUserEntity();
        user.setId(id);
        user.setAlias(name);
        user.setValidKey(protocol.toValidKey());
        user.setExpireTime(protocol.getExpireMillions());
        user.setAccessTime(System.currentTimeMillis());
        return user;
    }
}
