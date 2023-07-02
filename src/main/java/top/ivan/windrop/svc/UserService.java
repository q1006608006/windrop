package top.ivan.windrop.svc;

import top.ivan.windrop.bean.AccessUser;

import java.io.IOException;
import java.util.List;

/**
 * @author Ivan
 * @since 2023/06/30 13:48
 */
public interface UserService {

    AccessUser findUser(String id) throws IOException;

    List<AccessUser> findByAlias(String alias) throws IOException;

    AccessUser newUser(String id, String name, String validKey, int maxSecond) throws IOException;

    void updateUser(AccessUser user) throws IOException;

    void updateAccessTime(String id) throws IOException;

    void deleteUser(String id) throws IOException;

    void deleteAll() throws IOException;
}
