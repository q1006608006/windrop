package top.ivan.windrop.svc;

import lombok.extern.slf4j.Slf4j;
import top.ivan.windrop.bean.AccessUser;
import top.ivan.windrop.system.SystemUtils;
import top.ivan.windrop.system.io.WatchedFile;
import top.ivan.windrop.util.JSONUtils;

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
 * @description 用户信息service
 * @date 2021/3/10
 */
@Slf4j
public class PersistUserService implements UserService {
    private final WatchedFile fileHandler;

    private Map<String, AccessUser> userMap;

    public PersistUserService(String userDataFilePath) {
        fileHandler = new WatchedFile(userDataFilePath);
    }

    @Override
    public AccessUser findUser(String id) throws IOException {
        return takeUserMap().get(id);
    }

    @Override
    public List<AccessUser> findByAlias(String alias) throws IOException {
        return takeUserMap().values().stream().filter(u -> Objects.equals(alias, u.getAlias())).collect(Collectors.toList());
    }

    @Override
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
        saveToFile();
        return accessUser;
    }

    @Override
    public void updateUser(AccessUser user) throws IOException {
        if (null != user && findUser(user.getId()) != null) {
            takeUserMap().put(user.getId(), user);
            saveToFile();
        }
    }

    @Override
    public void updateAccessTime(String id) throws IOException {
        AccessUser user = findUser(id);
        if (null != user) {
            user.setAccessTime(System.currentTimeMillis());
            saveToFile();
        }
    }

    @Override
    public void deleteUser(String id) throws IOException {
        takeUserMap().remove(id);
        saveToFile();
    }

    @Override
    public void deleteAll() throws IOException {
        userMap = new ConcurrentHashMap<>();
        saveToFile();
    }

    private Map<String, AccessUser> takeUserMap() throws IOException {
        if (userMap == null || fileHandler.isUpdate()) {
            synchronized (this) {
                if (userMap == null || fileHandler.isUpdate()) {
                    updateUserMap();
                }
            }
        }
        return userMap;
    }

    private void updateUserMap() throws IOException {
        byte[] data = fileHandler.load();
        if (null == data) {
            userMap.clear();
            saveToFile();
        } else {
            try {
                data = SystemUtils.decrypt(data);
                BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data)));
                userMap = reader.lines().map(str -> JSONUtils.read(str, AccessUser.class)).collect(Collectors.toMap(AccessUser::getId, u -> u));
            } catch (Exception e) {
                log.error("", e);
                log.error("数据文件解密失败");
                userMap = new ConcurrentHashMap<>();
                saveToFile();
            } finally {
                fileHandler.sync();
            }
        }
    }

    private synchronized void saveToFile() throws IOException {
        Path path = fileHandler.getPath();
        List<String> lists = userMap.values().stream().map(JSONUtils::toString).collect(Collectors.toList());
        String data = String.join("\n", lists);
        byte[] bytes = SystemUtils.encrypt(data.getBytes());
        Files.deleteIfExists(path);
        Files.createFile(path);
        Files.write(path, bytes);
        fileHandler.sync();
    }

}
