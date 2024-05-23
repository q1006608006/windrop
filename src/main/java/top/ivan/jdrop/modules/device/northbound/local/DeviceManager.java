package top.ivan.jdrop.modules.device.northbound.local;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import top.ivan.jdrop.framework.boot.JDropConfig;
import top.ivan.jdrop.framework.exception.IllegalClientRequestException;
import top.ivan.jdrop.modules.device.domain.entity.ConnectProtocolEntity;
import top.ivan.jdrop.modules.device.domain.factory.ConnectProtocolFactory;
import top.ivan.jdrop.modules.device.domain.factory.DeviceFactory;
import top.ivan.jdrop.modules.device.northbound.local.assembler.DeviceConnectAssembler;
import top.ivan.jdrop.modules.device.southbound.ports.DeviceRepository;
import top.ivan.jdrop.modules.device.stable.module.dto.DeviceBindDTO;
import top.ivan.jdrop.modules.device.stable.module.dto.DeviceConnectDTO;
import top.ivan.jdrop.modules.device.stable.module.vo.DeviceBindVO;
import top.ivan.jdrop.framework.util.NetUtils;
import top.ivan.jdrop.framework.util.cache.LimitCache;

/**
 * @author Ivan
 * @since 2024/03/12 10:36
 */
@Slf4j
@Service
public class DeviceManager {

    private static final DeviceConnectAssembler Device_CONNECT_ASSEMBLER = new DeviceConnectAssembler();

    private final DeviceRepository deviceRepo;
    private final DeviceFactory deviceFactory;
    private final ConnectProtocolFactory connectProtocolFactory;
    private final JDropConfig config;
    private final LimitCache<ConnectProtocolEntity> protocolCache = new LimitCache<>(32);

    public DeviceManager(DeviceRepository deviceRepo,
                         DeviceFactory deviceFactory,
                         ConnectProtocolFactory connectProtocolFactory,
                         JDropConfig config) {
        this.deviceRepo = deviceRepo;
        this.deviceFactory = deviceFactory;
        this.connectProtocolFactory = connectProtocolFactory;
        this.config = config;
    }

    public Mono<DeviceBindDTO> bindDevice(DeviceBindVO bind) {
        return getProtocol(bind.getToken())
                .flatMap(p -> p.checkSign(bind) ? Mono.just(p) :
                        Mono.error(() -> new IllegalClientRequestException("核验失败，请检查网络安全")))
                .doOnNext(p -> deviceRepo.saveDevice(deviceFactory.newDevice(bind.getId(), bind.getName(), p)))
                .map(p -> new DeviceBindDTO(bind.getId(), p.getKeyElement()));
    }

    public Mono<DeviceConnectDTO> newDeviceConnect(int expireMillions) {
        return Mono.just(connectProtocolFactory.newConnectProtocol(expireMillions))
                .doOnNext(entity -> protocolCache.register(entity.getToken(),
                        () -> entity,
                        1,
                        expireMillions))
                .map(entity -> Device_CONNECT_ASSEMBLER.convertToDeviceConnect(entity,
                        NetUtils.getLocalHostName(),
                        config.getPort())
                );
    }

    public Mono<Void> removeConnectBind(DeviceConnectDTO uc) {
        if (null == uc) {
            return Mono.empty();
        }
        String token = ConnectProtocolEntity.buildToken(uc.getKeyRoot(), uc.getTokenElement());
        return Mono.fromRunnable(() -> protocolCache.remove(token));
    }

    private Mono<ConnectProtocolEntity> getProtocol(String token) {
        return Mono.defer(() -> {
            LimitCache.CacheData<ConnectProtocolEntity> protocol = protocolCache.getCacheData(token);
            if (protocol.isAccessible()) {
                return Mono.just(protocol.getData());
            } else {
                if (protocol.isLimit()) {
                    return Mono.error(new IllegalClientRequestException("超过访问次数"));
                } else if (protocol.isTimeout()) {
                    return Mono.error(new IllegalClientRequestException("已超时"));
                } else {
                    return Mono.error(new IllegalClientRequestException("无效请求"));
                }
            }
        }).doOnError(e -> log.error("get protocol failed: {}", e.getMessage()));
    }

}
