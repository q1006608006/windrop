package top.ivan.jardrop.user.infrastructure.repo.impl;

import org.springframework.stereotype.Component;
import top.ivan.jardrop.user.domain.AccessUserEntity;
import top.ivan.jardrop.user.infrastructure.repo.UserRepository;
import top.ivan.windrop.util.JSONUtils;

/**
 * @author Ivan
 * @since 2024/03/22 14:35
 */
@Component
public class UserRepositoryImpl implements UserRepository {


    @Override
    public void saveUser(AccessUserEntity user) {
        System.out.println(JSONUtils.toString(user));
    }
}
