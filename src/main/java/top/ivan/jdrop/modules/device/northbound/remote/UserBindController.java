package top.ivan.jdrop.modules.device.northbound.remote;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import top.ivan.jdrop.framework.model.ResponseVO;
import top.ivan.jdrop.modules.device.northbound.local.DeviceManager;
import top.ivan.jdrop.modules.device.stable.module.dto.DeviceBindDTO;
import top.ivan.jdrop.modules.device.stable.module.vo.DeviceBindVO;

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
    private DeviceManager deviceManager;

    @PostMapping("/bind")
    public Mono<ResponseVO<DeviceBindDTO>> bind(@RequestBody Mono<DeviceBindVO> m) {
        return m.cast(DeviceBindVO.class)
                .flatMap(uc -> deviceManager.bindDevice(uc))
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
