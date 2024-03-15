package top.ivan.jardrop.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.ivan.jardrop.common.cache.LimitCache;
import top.ivan.jardrop.user.Entity.AccessUser;
import top.ivan.jardrop.user.Entity.Protocol;
import top.ivan.jardrop.user.adapter.UserAdapter;
import top.ivan.jardrop.user.dto.UserBindDTO;

/**
 * @author Ivan
 * @since 2024/03/11 10:16
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private LimitCache cache;

    @Autowired
    private UserAdapter adapter;

    public AccessUser addUser(UserBindDTO bind, Protocol protocol) {
        AccessUser user = userRepo.newUser(bind.getId(), bind.getName(), protocol.toValidKey(), protocol.getExpireMillions());
        return user;
    }
}
