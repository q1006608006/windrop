package top.ivan.jdrop.modules.device.northbound.local;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import top.ivan.jdrop.framework.exception.IllegalClientRequestException;
import top.ivan.jdrop.modules.device.domain.AccessService;
import top.ivan.jdrop.modules.device.stable.module.dto.AccessApplyDTO;
import top.ivan.jdrop.modules.device.stable.module.vo.AccessInfoVO;

import javax.annotation.Resource;

/**
 * @author Ivan
 * @since 2024/03/29 00:39
 */
@Service
public class AccessManager {

    @Resource
    private AccessService accessService;

    /**
     * apply access for device
     *
     * @param info
     * @return
     */
    public Mono<AccessApplyDTO> accessApply(AccessInfoVO info) {
        return accessService.isDeviceAccessible(info.getDeviceId())
                .flatMap(pass -> pass ? accessService.newAccessToken(info) :
                        Mono.error(new IllegalClientRequestException("无权限访问")))
                .then(accessService.newAccessToken(info))
                .map(AccessApplyDTO::new);
    }

    public Mono<AccessInfoVO> getAccessInfo(String ticket) {
        return accessService.getAccessInfo(ticket)
                .switchIfEmpty(Mono.error(new IllegalClientRequestException("未验证的请求")));
    }

    public Mono<Boolean> isAccessible(String ticket) {
        return accessService.isAccessible(ticket);
    }

}
