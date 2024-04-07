package top.ivan.jdrop.user.infrastructure.repo;

import org.springframework.stereotype.Component;
import top.ivan.jdrop.user.domain.model.AccessUserEntity;
import top.ivan.jdrop.user.domain.repo.UserRepository;
import top.ivan.windrop.util.JSONUtils;

/**
 * @author Ivan
 * @since 2024/03/22 14:35
 */
@Component
public class UserRepositoryImpl implements UserRepository {

    @Override
    public void saveUser(AccessUserEntity user) {
        //todo
        System.out.println(JSONUtils.toString(user));
    }

    @Override
    public AccessUserEntity findUser(String id) {
        //todo
        return new AccessUserEntity();
    }
}
