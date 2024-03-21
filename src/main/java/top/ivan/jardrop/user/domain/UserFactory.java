package top.ivan.jardrop.user.domain;

/**
 * @author Ivan
 * @since 2024/03/20 17:22
 */
public class UserFactory {
    public AccessUserEntity newUser(String id, String name, String validKey, long expiredMillions) {
        AccessUserEntity user = new AccessUserEntity();
        user.setId(id);
        user.setAlias(name);
        user.setValidKey(validKey);
        user.setExpireTime(expiredMillions);
        user.setAccessTime(System.currentTimeMillis());
        return user;
    }
}
