package top.ivan.windrop.svc;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import top.ivan.windrop.bean.AccessUser;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ivan
 * @since 2023/08/26 09:17
 */
class WindropManageServiceTest {

    private static final Map<String,AccessUser> userMap = new HashMap<>();

    static {
        userMap.put("EFFECTIVE", new AccessUser());
    }

    private static WindropManageService service;

    @BeforeAll
    public static void init() {
/*        MockUserService mock = new MockUserService();
        UserService userService = Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader()
                , new Class[]{UserService.class}
                , ((proxy, method, args) -> {
                    if (UserService.class.getMethod())
                })
        );

        service = new WindropManageService();*/

    }

    @Test
    void applyKey() {

    }

    @Test
    void updateClipboard() {
    }

    private static final class MockUserService {
        public final Map<String, AccessUser> userMap;

        private MockUserService(Map<String, AccessUser> userMap) {
            this.userMap = userMap;
        }

        public AccessUser findUser(String userId) {
            return userMap.get(userId);
        }

        public <T> T unsupport() {
            throw new RuntimeException("un support in mocked instance");
        }
    }

/*    private static AccessUser buildUser(String id,String ) {

    }*/
}