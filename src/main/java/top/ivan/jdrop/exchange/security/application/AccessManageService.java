package top.ivan.jdrop.exchange.security.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import top.ivan.jdrop.common.constant.OperateType;
import top.ivan.jdrop.common.util.JSONUtils;
import top.ivan.jdrop.common.util.WrapperUtils;
import top.ivan.jdrop.exchange.common.vo.ExchangeVO;
import top.ivan.jdrop.exchange.security.application.dto.ApplyDTO;
import top.ivan.jdrop.exchange.security.domain.AuthService;
import top.ivan.jdrop.exchange.security.domain.ValidService;
import top.ivan.jdrop.exchange.security.userinterface.vo.ApplyVO;

/**
 * @author Ivan
 * @since 2024/03/29 00:39
 */
@Service
public class AccessManageService {

    @Autowired
    private ValidService validService;

    @Autowired
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
