package top.ivan.jdrop.user.application;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import top.ivan.jdrop.exchange.application.dto.ApplyDTO;
import top.ivan.jdrop.exchange.userinterface.vo.security.ApplyVO;
import top.ivan.jdrop.framework.constant.OperateType;
import top.ivan.jdrop.framework.module.ExchangeVO;
import top.ivan.jdrop.tool.JSONUtils;
import top.ivan.jdrop.user.domain.security.AuthService;
import top.ivan.jdrop.user.domain.security.ValidService;

import javax.annotation.Resource;

/**
 * @author Ivan
 * @since 2024/03/29 00:39
 */
@Service
public class AccessManager {

    @Resource
    private ValidService validService;

    @Resource
    private AuthService authService;

    public Mono<ApplyDTO> newApply(ApplyVO vo) {
        return Mono.just(validService.takeValidToken(vo.getUserId()))
                .doFirst(() -> authService.register(vo.getUserId(), vo.getOperate(), vo.getMediaType()))
                .map(ApplyDTO::new);
    }

    public Mono<Boolean> tryAccess(ExchangeVO<?> exchange, OperateType operate) {
        return Mono.just(authService.checkAccessible(exchange.getId(), operate, exchange.getMediaType()))
                .filter(result -> true)
                .map(ignore -> validService.valid(exchange.getId(),
                        exchange.getToken(),
                        JSONUtils.toString(exchange.getData()))
                ).switchIfEmpty(Mono.just(false));
    }
}
