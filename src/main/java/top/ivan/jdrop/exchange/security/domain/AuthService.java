package top.ivan.jdrop.exchange.security.domain;

import org.springframework.stereotype.Service;
import top.ivan.jdrop.common.cache.LimitCache;
import top.ivan.jdrop.common.constant.MediaType;
import top.ivan.jdrop.common.constant.OperateType;

import java.util.Objects;

/**
 * @author Ivan
 * @since 2024/03/29 10:24
 */
@Service
public class AuthService {

    private final LimitCache<String> groupCache = new LimitCache<>();

    public void register(String userId, OperateType operate, MediaType media) {
        groupCache.register(userId, () -> buildPermission(operate, media), 1, 30000);
    }

    public boolean checkAccessible(String userId, OperateType operateType, MediaType type) {
        LimitCache.CacheData<String> permissionData = groupCache.getCacheData(userId);
        if (permissionData.isAccessible()) {
            return Objects.equals(permissionData.getData(), buildPermission(operateType, type));
        }
        return false;
    }

    private String buildPermission(OperateType operate, MediaType media) {
        return String.join("<>", operate.name(), media.name());
    }
}
