package top.ivan.windrop.ctl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import top.ivan.windrop.bean.*;
import top.ivan.windrop.clip.ClipBean;
import top.ivan.windrop.ex.HttpClientException;
import top.ivan.windrop.svc.WindropManageService;
import top.ivan.windrop.util.ClipUtil;
import top.ivan.windrop.verify.VerifyIP;

import java.io.IOException;

/**
 * @author Ivan
 * @description windrop核心控制器
 * @date 2020/12/17
 */
@Slf4j
@RestController
@RequestMapping("/windrop")
public class SwapController {

    @Autowired
    private WindropManageService manageService;

    /**
     * 申请校验码
     *
     * @param request 推送数据{@link ApplyRequest}
     * @return 随机访问密钥 {@link ApplyResponse}
     */
    @PostMapping("apply")
    @VerifyIP
    public Mono<ApplyResponse> apply(@RequestBody ApplyRequest request) {
        return takeClipBean()
                .flatMap(bean -> manageService.takeValidKey(request, bean))
                .map(ApplyResponse::success);
    }

    /**
     * 服务端接收内容，写入剪贴板
     *
     * @param request 数据{@link WindropRequest}
     * @return 更新结果
     */
    @PostMapping("push")
    @VerifyIP
    public Mono<CommonResponse> accept(@RequestBody WindropRequest request) {
        // 校验data
        if (null == request.getData()) {
            log.error("request without 'data'");
            return Mono.error(new HttpClientException(HttpStatus.BAD_REQUEST, "异常请求"));
        }

        return manageService.updateClipboard(request)
                .then(Mono.just(CommonResponse.success("更新成功")));
    }

    /**
     * 请求服务端剪贴板
     *
     * @param req 拉取请求数据{@link WindropRequest}
     * @return 本地剪切板内容或大文件resourceId
     */
    @PostMapping("pull")
    @VerifyIP
    public Mono<WindropResponse> takeClipboard(@RequestBody WindropRequest req) {
        return takeClipBean().flatMap(bean -> manageService.quickResponse(req, bean, true));
    }

    private Mono<ClipBean> takeClipBean() {
        return Mono.defer(() -> {
            try {
                return Mono.just(ClipUtil.getClipBean());
            } catch (IOException e) {
                return Mono.error(e);
            }
        });
    }


}
