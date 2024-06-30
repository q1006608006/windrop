package top.ivan.jdrop.modules.device.northbound.local;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import top.ivan.jdrop.framework.constant.Operate;
import top.ivan.jdrop.framework.exception.IllegalClientRequestException;
import top.ivan.jdrop.modules.device.domain.AccessService;
import top.ivan.jdrop.modules.device.stable.module.dto.AccessApplyDTO;
import top.ivan.jdrop.modules.device.stable.module.vo.AccessInfoVO;
import top.ivan.jdrop.modules.exchange.stable.vo.UploadInfoVO;

import javax.annotation.Resource;
import java.util.stream.Collectors;

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
     * @param operate
     * @param info
     * @return
     */
    public Mono<AccessApplyDTO> accessApply(UploadInfoVO info, Operate operate) {
        return accessService.isDeviceAccessible(info.getDeviceId())
                .flatMap(pass -> pass ? newAccess(info, operate) :
                        Mono.error(new IllegalClientRequestException("无权限访问")))
                .map(AccessInfoVO::getToken)
                .map(AccessApplyDTO::new);
    }

    public Mono<AccessInfoVO> getAccessInfo(String ticket) {
        return accessService.getAccessInfo(ticket)
                .switchIfEmpty(Mono.error(new IllegalClientRequestException("未验证的请求")));
    }

    public Mono<Boolean> isAccessible(String ticket) {
        return accessService.isAccessible(ticket);
    }

    public Mono<AccessInfoVO> newAccess(UploadInfoVO vo, Operate operate) {
        AccessInfoVO info = new AccessInfoVO(vo.getDeviceId(),
                operate,
                vo.getMediaType(),
                vo.getDigests().stream().collect(Collectors.joining("&&")));

        return accessService.applyAccessToken(info)
                .doOnNext(info::setToken)
                .thenReturn(info);
    }

}
