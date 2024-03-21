package top.ivan.jardrop.user.domain;


import top.ivan.jardrop.user.domain.AccessUserEntity;

import java.util.List;

/**
 * @author Ivan
 * @since 2023/06/30 13:48
 */
public interface UserRepository {

    AccessUserEntity findUser(String id);

    List<AccessUserEntity> findByAlias(String alias);

    void saveUser(AccessUserEntity user);

    void updateUser(AccessUserEntity user);

    void updateAccessTime(String id);

    void deleteUser(String id);

    void deleteAll();
}
