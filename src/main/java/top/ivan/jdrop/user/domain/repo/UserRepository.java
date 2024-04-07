package top.ivan.jdrop.user.domain.repo;


import top.ivan.jdrop.user.domain.model.AccessUserEntity;

/**
 * @author Ivan
 * @since 2023/06/30 13:48
 */
public interface UserRepository {

    void saveUser(AccessUserEntity user);

    AccessUserEntity findUser(String id);

}
