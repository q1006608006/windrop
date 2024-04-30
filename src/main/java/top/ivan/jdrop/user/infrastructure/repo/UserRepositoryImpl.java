package top.ivan.jdrop.user.infrastructure.repo;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.ivan.jdrop.tool.cache.VolatileHandler;
import top.ivan.jdrop.tool.fs.EncryptFile;
import top.ivan.jdrop.tool.IDUtils;
import top.ivan.jdrop.tool.JSONUtils;
import top.ivan.jdrop.user.domain.model.AccessUserEntity;
import top.ivan.jdrop.user.domain.repo.UserRepository;
import top.ivan.jdrop.user.infrastructure.DateUtils;
import top.ivan.windrop.system.SystemUtils;

import java.io.IOException;
import java.util.Map;

/**
 * @author Ivan
 * @since 2024/03/22 14:35
 */
@Slf4j
@Component
public class UserRepositoryImpl implements UserRepository {
    private static final String BASE_DATA_ROOT = SystemUtils.getDocumentsPath() + "/jdrop/u.data";

    private final EncryptFile dataFile;

    private final VolatileHandler<Map<String, AccessUserEntity>> mapHandler;

    public UserRepositoryImpl() {
        dataFile = new EncryptFile(BASE_DATA_ROOT, IDUtils.getDeviceID());
        this.mapHandler = new VolatileHandler<>();
        initDataFile();
    }

    @Override
    public void saveUser(AccessUserEntity user) {
        Map<String, AccessUserEntity> map = getAllUser();
        map.put(user.getId(), user);
        saveUser(map);
        log.info("add new user: '{}'({}), expired in {}", user.getAlias(), user.getId(), DateUtils.timestampToString(user.getExpireTime()));
    }

    @Override
    public AccessUserEntity findUser(String id) {
        return getAllUser().get(id);
    }

    private void initDataFile() {
        if (!dataFile.isExist()) {
            try {
                log.info("no data file found, create new.");
                dataFile.create();
                getAllUser();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Map<String, AccessUserEntity> getAllUser() {
        if (!dataFile.isUpdate() && mapHandler.hasValue()) {
            return mapHandler.getValue();
        } else {
            try {
                if (!dataFile.isExist()) {
                    dataFile.create();
                }
                byte[] data = dataFile.load();
                String content = data == null || data.length == 0 ? "{}" : new String(data);
                Map<String, AccessUserEntity> map = JSONUtils.read(content, new TypeReference<Map<String, AccessUserEntity>>() {
                });
                mapHandler.setValue(map);
                return map;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void saveUser(Map<String, AccessUserEntity> map) {
        this.mapHandler.setValue(map);
        try {
            dataFile.write(JSONUtils.toString(map).getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
