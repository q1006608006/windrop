package top.ivan.windrop.svc;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.ivan.windrop.util.ConvertUtil;

import java.util.Objects;

/**
 * @author Ivan
 * @since 2021/07/13 10:45
 */
@Service
@Slf4j(topic = "valid")
public class ValidService {
    @Autowired
    private RandomAccessKeyService keyService;

    public boolean validSign(String group, String sign, String... contents) {
        return keyService.match(group, key -> Objects.equals(sign, ConvertUtil.combines(true, ";", key, contents)));
    }

    public String getValidKey(String group, int timeout) {
        return keyService.getKey(group, timeout);
    }

}
