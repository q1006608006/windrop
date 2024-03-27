package top.ivan.jdrop.user.infrastructure.repo;


import top.ivan.jdrop.user.domain.AccessUserEntity;

/**
 * @author Ivan
 * @since 2023/06/30 13:48
 */
public interface UserRepository {

    void saveUser(AccessUserEntity user);

}
