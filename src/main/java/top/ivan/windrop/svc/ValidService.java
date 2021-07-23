package top.ivan.windrop.svc;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import top.ivan.windrop.util.ConvertUtil;
import top.ivan.windrop.verify.WebHandler;

import java.util.Objects;

/**
 * @author Ivan
 * @since 2021/07/13 10:45
 */
@Service
public class ValidService {
    @Autowired
    private RandomAccessKeyService keyService;

    public Mono<Boolean> valid(String group, String sign, String... patterns) {
        return WebHandler.ip().map(ip -> keyService.match(group, key -> {
            String content = ConvertUtil.combines(";", key, ip, patterns);
            return Objects.equals(sign, DigestUtils.sha256Hex(content));
        }));
    }

    public String getValidKey(String group, int timeout) {
        return keyService.getKey(group, timeout);
    }

}
