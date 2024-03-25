package top.ivan.jardrop.user.userinterface;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import top.ivan.jardrop.common.module.ResponseVO;
import top.ivan.jardrop.user.application.UserAddService;
import top.ivan.jardrop.user.userinterface.module.dto.UserBindDTO;
import top.ivan.jardrop.user.userinterface.module.vo.UserBindVO;

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

    @RequestMapping("show")
    public Mono<Void> show() {
        return userAddService.showConnectQrCode(60 * 1000 * 30).then();
    }
}
