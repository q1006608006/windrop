package top.ivan.windrop.svc;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import top.ivan.windrop.bean.AccessUser;
import top.ivan.windrop.util.SystemUtil;
import top.ivan.windrop.util.WatchedFile;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Ivan
 * @description
 * @date 2021/3/10
 */
@Slf4j
public class PersistUserService {
    private final WatchedFile fileHandler;

    private Map<String, AccessUser> userMap;

    public PersistUserService(String userDataFilePath) {
        fileHandler = new WatchedFile(userDataFilePath);
    }

    public AccessUser findUser(String id) throws IOException {
        return takeUserMap().get(id);
    }

    public List<AccessUser> findByAlias(String alias) throws IOException {
        return takeUserMap().values().stream().filter(u -> Objects.equals(alias, u.getAlias())).collect(Collectors.toList());
    }

    public AccessUser newUser(String id, String name, String validKey, int maxSecond) throws IOException {
        AccessUser accessUser = new AccessUser();
        accessUser.setId(id);
        accessUser.setAlias(name);
        accessUser.setValidKey(validKey);
        accessUser.setAccessTime(System.currentTimeMillis());
        if (maxSecond > -1) {
            accessUser.setExpireTime(accessUser.getAccessTime() + maxSecond * 1000L);
        } else {
            accessUser.setExpireTime(-1);
        }
        takeUserMap().put(id, accessUser);
        saveUserMap();
        return accessUser;
    }

    public void updateUser(AccessUser user) throws IOException {
        if (null != user && findUser(user.getId()) != null) {
            takeUserMap().put(user.getId(), user);
            saveUserMap();
        }
    }

    public void updateAccessTime(String id) throws IOException {
        AccessUser user = findUser(id);
        if (null != user) {
            user.setAccessTime(System.currentTimeMillis());
            saveUserMap();
        }
    }

    public void deleteUser(String id) throws IOException {
        takeUserMap().remove(id);
        saveUserMap();
    }

    public void deleteAll() throws IOException {
        if(null != userMap) {
            userMap.clear();
            saveUserMap();
        }
    }

    private Map<String, AccessUser> takeUserMap() throws IOException {
        if (userMap == null || fileHandler.isUpdate()) {
            synchronized (this) {
                byte[] data = fileHandler.load();
                if (null == data) {
                    userMap.clear();
                    saveUserMap();
                } else {
                    try {
                        data = SystemUtil.decrypt(data);
                    } catch (Exception e) {
                        log.error("", e);
                        log.error("尝试解密文件失败，可能的情况为该应用是由其他机器移植而来！");
                        userMap = new ConcurrentHashMap<>();
                        saveUserMap();
                        return userMap;
                    }
                    BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data)));
                    userMap = reader.lines().map(str -> JSONObject.parseObject(str, AccessUser.class)).collect(Collectors.toMap(AccessUser::getId, u -> u));
                    fileHandler.sync();
                }
            }
        }
        return userMap;
    }

    private synchronized void saveUserMap() throws IOException {
        Path path = fileHandler.getPath();
        List<String> lists = userMap.values().stream().map(JSONObject::toJSONString).collect(Collectors.toList());
        String data = String.join("\n", lists);
        byte[] bytes = SystemUtil.encrypt(data.getBytes());
        Files.deleteIfExists(path);
        Files.createFile(path);
        Files.write(path, bytes);
        fileHandler.sync();
    }

}
