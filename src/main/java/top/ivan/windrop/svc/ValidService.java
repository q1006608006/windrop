package top.ivan.windrop.svc;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j(topic = "val" + "id")
public class ValidService {
    @Autowired
    private RandomAccessKeyService keyService;

    public Mono<Boolean> valid(String group, String sign, String... patterns) {
        return WebHandler.ip().map(ip -> keyService.match(group, key -> {
            if (log.isInfoEnabled()) {
                log.info("val" + "id {}-{}-{}", group, ip, "[" + String.join(",", patterns) + "]");
            }
            String content = ConvertUtil.combines(";", key, ip, patterns);
            return Objects.equals(sign, DigestUtils.sha256Hex(content));
        }));
    }

    public String getValidKey(String group, int timeout) {
        return keyService.getKey(group, timeout);
    }

}
