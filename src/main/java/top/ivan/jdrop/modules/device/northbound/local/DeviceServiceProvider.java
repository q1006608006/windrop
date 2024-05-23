package top.ivan.jdrop.modules.device.northbound.local;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import top.ivan.jdrop.framework.exception.IllegalClientRequestException;
import top.ivan.jdrop.modules.device.domain.entity.DeviceEntity;
import top.ivan.jdrop.modules.device.southbound.ports.DeviceRepository;

/**
 * @author Ivan
 * @since 2024/03/27 17:11
 */
@Slf4j
@Service
public class DeviceServiceProvider {

    @Autowired
    private DeviceRepository deviceRepository;

    public Mono<Boolean> validSign(String id, String sign, String content) {
        return findDevice(id).map(device -> device.validContent(content, sign));
    }

    public Mono<String> sign(String id, String content) {
        return findDevice(id).map(device -> device.sign(content));
    }

    private Mono<DeviceEntity> findDevice(String id) {
        return Mono.just(deviceRepository.findDevice(id))
                .flatMap(opt -> opt.map(Mono::just)
                        .getOrElse(Mono.error(new IllegalClientRequestException("设备不存在: " + id))));
    }
}
