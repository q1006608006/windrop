package top.ivan.jdrop.modules.device.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import top.ivan.jdrop.framework.exception.IllegalClientRequestException;
import top.ivan.jdrop.modules.device.domain.entity.DeviceEntity;
import top.ivan.jdrop.modules.device.southbound.ports.DeviceRepository;
import top.ivan.jdrop.modules.device.stable.module.vo.AccessInfoVO;
import top.ivan.jdrop.framework.util.IDUtils;
import top.ivan.jdrop.framework.util.cache.LimitCache;

@Slf4j
@Service
public class AccessService {
    private final DeviceRepository deviceRepository;

    public AccessService(DeviceRepository repo) {
        this.deviceRepository = repo;
    }

    private final LimitCache<AccessInfoVO> cache = new LimitCache<>(32);

    /**
     * apply valid token for next access
     *
     * @param info access info
     * @return session valid token
     */
    public Mono<String> newAccessToken(AccessInfoVO info) {
        return Mono.just(IDUtils.get32UUID())
                .flatMap(token -> buildAccessKey(token, info)
                        .doOnNext(key -> cache.register(key, () -> info, 1, 60 * 1000))
                        .thenReturn(token));
    }

    /**
     * use access token get access info
     * <p>
     * digest for ticket: {@link #buildAccessKey(String, AccessInfoVO)}
     *
     * @param ticket
     * @return
     */
    public Mono<AccessInfoVO> getAccessInfo(String ticket) {
        return Mono.just(cache.getCacheData(ticket))
                .filter(LimitCache.CacheData::isAccessible)
                .map(LimitCache.CacheData::getData);
    }

    public Mono<Boolean> isDeviceAccessible(String deviceId) {
        return Mono.just(deviceRepository.findDevice(deviceId).isEmpty());
    }

    public Mono<Boolean> isAccessible(String ticket) {
        return Mono.just(cache.exist(ticket));
    }

    /**
     * build key for access
     * method: sha256(token, digest, password)
     *
     * @param token
     * @param info
     * @return
     */
    private Mono<String> buildAccessKey(String token, AccessInfoVO info) {
        return getDevice(info.getDeviceId())
                .map(device -> device.sign(String.join(",", token, info.getDigest())));
    }

    private Mono<DeviceEntity> getDevice(String id) {
        return deviceRepository.findDevice(id)
                .map(Mono::just)
                .getOrElse(() -> Mono.fromRunnable(() -> log.error("not found device: {}", id))
                        .then(Mono.error(new IllegalClientRequestException("device not found"))));
    }

}
