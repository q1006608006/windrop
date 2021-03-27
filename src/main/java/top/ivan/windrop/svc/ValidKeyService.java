package top.ivan.windrop.svc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.ivan.windrop.bean.WindropConfig;

import java.util.UUID;

/**
 * @author Ivan
 * @description
 * @date 2021/2/7
 */
@Slf4j
@Service
public class ValidKeyService {

    private WindropConfig config;

    private String validKey;

    @Autowired
    public void setConfig(WindropConfig config) {
        this.config = config;
    }

    public String getValidKey() {
        if (StringUtils.isEmpty(config.getValidKey())) {
            if (StringUtils.isEmpty(validKey)) {
                validKey = UUID.randomUUID().toString().replace("-", "");
            }
            return validKey;
        } else {
            return config.getValidKey();
        }
    }

}
