package top.ivan.jdrop.user.domain.factory;

import org.springframework.stereotype.Component;
import top.ivan.jdrop.user.domain.model.AccessUserEntity;
import top.ivan.jdrop.user.domain.model.ConnectProtocolEntity;

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
        user.setExpireTime(protocol.nextExpireTime());
        user.setAccessTime(System.currentTimeMillis());
        return user;
    }
}
