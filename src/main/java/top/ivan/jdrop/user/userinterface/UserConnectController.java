package top.ivan.jdrop.user.userinterface;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import top.ivan.jdrop.common.module.ResponseVO;
import top.ivan.jdrop.user.application.UserAddService;
import top.ivan.jdrop.user.userinterface.model.dto.UserBindDTO;
import top.ivan.jdrop.user.userinterface.model.vo.UserBindVO;

/**
 * @author Ivan
 * @description 设备连接windrop的控制器
 * @date 2021/2/5
 */
@Slf4j
@RestController
@RequestMapping("/jardrop")
public class UserConnectController {

    @Autowired
    private UserAddService userAddService;

    @PostMapping("/bind")
    public Mono<ResponseVO<UserBindDTO>> bind(@RequestBody Mono<UserBindVO> m) {
        return m.cast(UserBindVO.class)
                .flatMap(uc -> userAddService.bindUser(uc))
                .map(ResponseVO::success);
    }

    //todo remove after develop finished
    @Autowired
    private UserAddWindow userAddWindow;

    public Mono<Void> show() {
        userAddWindow.newConnectWindow(60 * 1000 * 30);
        return Mono.never();
    }
}
