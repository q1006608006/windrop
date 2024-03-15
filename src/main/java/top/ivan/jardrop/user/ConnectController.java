package top.ivan.jardrop.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import top.ivan.jardrop.common.vo.ResponseVO;
import top.ivan.jardrop.user.application.UserAddApp;
import top.ivan.jardrop.user.dto.UserBindDTO;
import top.ivan.jardrop.user.vo.UserBindResponse;
import top.ivan.jardrop.user.vo.UserBindVO;

/**
 * @author Ivan
 * @description 设备连接windrop的控制器
 * @date 2021/2/5
 */
@Slf4j
@RestController
@RequestMapping("/jardrop/device")
public class ConnectController {


    @Autowired
    private UserAddApp userAddApp;

    @RequestMapping("/bind")
    public Mono<ResponseVO<UserBindResponse>> bind(Mono<UserBindVO> m) {
        return m.cast(UserBindDTO.class)
                .flatMap(uc -> userAddApp.bindUser(uc))
                .map(ResponseVO::success);
    }
}
