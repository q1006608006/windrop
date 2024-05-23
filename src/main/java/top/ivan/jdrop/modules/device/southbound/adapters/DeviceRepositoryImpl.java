package top.ivan.jdrop.modules.device.southbound.adapters;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Option;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.ivan.jdrop.framework.util.IDUtils;
import top.ivan.jdrop.framework.util.JSONUtils;
import top.ivan.jdrop.framework.util.cache.VolatileHandler;
import top.ivan.jdrop.framework.util.fs.EncryptFile;
import top.ivan.jdrop.modules.device.domain.entity.DeviceEntity;
import top.ivan.jdrop.modules.device.southbound.ports.DeviceRepository;
import top.ivan.jdrop.modules.device.stable.DateUtils;
import top.ivan.windrop.system.SystemUtils;

import java.io.IOException;
import java.util.Map;

/**
 * @author Ivan
 * @since 2024/03/22 14:35
 */
@Slf4j
@Component
public class DeviceRepositoryImpl implements DeviceRepository {
    private static final String BASE_DATA_ROOT = SystemUtils.getDocumentsPath() + "/jdrop/u.data";

    private final EncryptFile dataFile;

    private final VolatileHandler<Map<String, DeviceEntity>> mapHandler;

    public DeviceRepositoryImpl() {
        dataFile = new EncryptFile(BASE_DATA_ROOT, IDUtils.getDeviceID());
        this.mapHandler = new VolatileHandler<>();
        initDataFile();
    }

    @Override
    public void saveDevice(DeviceEntity user) {
        Map<String, DeviceEntity> map = getAllUser();
        map.put(user.getId(), user);
        saveUser(map);
        log.info("add new user: '{}'({}), expired in {}", user.getAlias(), user.getId(), DateUtils.timestampToString(user.getExpireTime()));
    }

    @Override
    public Option<DeviceEntity> findDevice(String id) {
        return Option.of(getAllUser().get(id));
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

    private Map<String, DeviceEntity> getAllUser() {
        if (!dataFile.isUpdate() && mapHandler.hasValue()) {
            return mapHandler.getValue();
        } else {
            try {
                if (!dataFile.isExist()) {
                    dataFile.create();
                }
                byte[] data = dataFile.load();
                String content = data == null || data.length == 0 ? "{}" : new String(data);
                Map<String, DeviceEntity> map = JSONUtils.read(content, new TypeReference<Map<String, DeviceEntity>>() {
                });
                mapHandler.setValue(map);
                return map;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void saveUser(Map<String, DeviceEntity> map) {
        this.mapHandler.setValue(map);
        try {
            dataFile.write(JSONUtils.toString(map).getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
