package top.ivan.jdrop.exchange.userinterface;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import top.ivan.jdrop.exchange.application.ApplyService;
import top.ivan.jdrop.exchange.userinterface.model.dto.ApplyDTO;
import top.ivan.jdrop.exchange.userinterface.model.vo.ApplyVO;

/**
 * @author Ivan
 * @since 2024/03/29 00:23
 */
@RestController
public class ApplyController {
    @Autowired
    private ApplyService applyService;

    public Mono<ApplyDTO> apply(Mono<ApplyVO> mono) {
        //todo
        return Mono.empty();
    }
}
