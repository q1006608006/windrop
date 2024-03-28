package top.ivan.jdrop.user.infrastructure.factory;

import org.springframework.stereotype.Component;
import top.ivan.jdrop.user.domain.ConnectProtocolEntity;

/**
 * @author Ivan
 * @since 2024/03/27 16:31
 */
@Component
public class ConnectProtocolFactory {

    public ConnectProtocolEntity newConnectProtocol(int expireMillions) {
        return new ConnectProtocolEntity(expireMillions);
    }

}
