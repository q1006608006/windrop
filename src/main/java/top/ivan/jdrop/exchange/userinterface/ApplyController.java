package top.ivan.jdrop.exchange.userinterface;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import top.ivan.jdrop.framework.module.ResponseVO;
import top.ivan.jdrop.user.application.AccessManager;
import top.ivan.jdrop.exchange.application.dto.ApplyDTO;
import top.ivan.jdrop.exchange.userinterface.vo.security.ApplyVO;

/**
 * @author Ivan
 * @since 2024/03/29 00:23
 */
@RestController
public class ApplyController {
    @Autowired
    private AccessManager accessManager;

    @RequestMapping
    public Mono<ResponseVO<ApplyDTO>> apply(Mono<ApplyVO> mono) {
        return mono.flatMap(accessManager::newApply)
                .map(ResponseVO::success);
    }
}
