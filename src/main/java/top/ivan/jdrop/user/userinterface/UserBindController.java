package top.ivan.jdrop.user.userinterface;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import top.ivan.jdrop.framework.module.ResponseVO;
import top.ivan.jdrop.user.application.UserManager;
import top.ivan.jdrop.user.application.dto.UserBindDTO;
import top.ivan.jdrop.user.userinterface.vo.UserBindVO;

/**
 * @author Ivan
 * @description 设备连接windrop的控制器
 * @date 2021/2/5
 */
@Slf4j
@RestController
@RequestMapping("/jdrop")
public class UserBindController {

    @Autowired
    private UserManager userManager;

    @PostMapping("/bind")
    public Mono<ResponseVO<UserBindDTO>> bind(@RequestBody Mono<UserBindVO> m) {
        return m.cast(UserBindVO.class)
                .flatMap(uc -> userManager.bindUser(uc))
                .map(ResponseVO::success);
    }

    //todo remove after develop finished
    @Autowired
    private UserAddWindow userAddWindow;

    @GetMapping("show")
    public Mono<Void> show() {
        userAddWindow.newConnectWindow(60 * 1000 * 30);
        return Mono.never();
    }
}
