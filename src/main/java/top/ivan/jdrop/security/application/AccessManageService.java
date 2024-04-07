package top.ivan.jdrop.security.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import top.ivan.jdrop.security.domain.AuthService;
import top.ivan.jdrop.security.domain.ValidService;
import top.ivan.jdrop.security.userinterface.model.dto.ApplyDTO;
import top.ivan.jdrop.security.userinterface.model.vo.ApplyVO;

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
        return Mono.fromRunnable(() -> authService.register(vo.getUserId(), vo.getOperate(), vo.getMediaType()))
                .then(Mono.defer(() -> validService.takeValidToken(vo.getUserId())))
                .map(ApplyDTO::new);
    }
}
