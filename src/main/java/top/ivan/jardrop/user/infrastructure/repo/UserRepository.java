package top.ivan.jardrop.user.infrastructure.repo;


import top.ivan.jardrop.user.domain.AccessUserEntity;

/**
 * @author Ivan
 * @since 2023/06/30 13:48
 */
public interface UserRepository {

    void saveUser(AccessUserEntity user);

}
