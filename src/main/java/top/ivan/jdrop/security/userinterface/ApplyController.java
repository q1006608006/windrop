package top.ivan.jdrop.security.userinterface;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import top.ivan.jdrop.common.module.ResponseVO;
import top.ivan.jdrop.security.application.AccessManageService;
import top.ivan.jdrop.security.userinterface.model.dto.ApplyDTO;
import top.ivan.jdrop.security.userinterface.model.vo.ApplyVO;

/**
 * @author Ivan
 * @since 2024/03/29 00:23
 */
@RestController
public class ApplyController {
    @Autowired
    private AccessManageService accessManageService;

    public Mono<ResponseVO<ApplyDTO>> apply(Mono<ApplyVO> mono) {
        return mono.flatMap(accessManageService::newApply)
                .map(ResponseVO::success);
    }
}
