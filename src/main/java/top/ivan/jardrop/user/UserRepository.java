package top.ivan.jardrop.user;


import top.ivan.jardrop.user.Entity.AccessUser;

import java.util.List;

/**
 * @author Ivan
 * @since 2023/06/30 13:48
 */
public interface UserRepository {

    AccessUser findUser(String id);

    List<AccessUser> findByAlias(String alias);

    AccessUser newUser(String id, String name, String validKey, long expiredMillions);

    void updateUser(AccessUser user);

    void updateAccessTime(String id);

    void deleteUser(String id);

    void deleteAll();
}
