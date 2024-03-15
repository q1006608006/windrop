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
import top.ivan.windrop.system.clipboard.ClipBean;
import top.ivan.windrop.exception.HttpClientException;
import top.ivan.windrop.svc.SecurityManager;
import top.ivan.windrop.svc.WindropManageService;
import top.ivan.windrop.system.ClipUtils;
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
    private static final String OPERATE_PULL = "PULL";

    @Autowired
    private WindropManageService manageService;

    @Autowired
    private SecurityManager security;

    /**
     * 申请校验码
     * #1 判断请求类型
     * #2 根据请求类型请求验证key
     * #3 包装为标准返回对象
     *
     * @param request 推送数据{@link ApplyRequest}
     * @return 随机访问密钥 {@link ApplyResponse}
     */
    @PostMapping("apply")
    @VerifyIP
    public Mono<ApplyResponse> apply(@RequestBody ApplyRequest request) {
        return security.buildValidKey(request)
                .map(ApplyResponse::success);
/*        return manageService.applyKey(request)
                .map(ApplyResponse::success);*/
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
        if (null == request.getContent()) {
            log.error("request without 'data'");
            return Mono.error(new HttpClientException(HttpStatus.BAD_REQUEST, "异常请求"));
        }

        return security.valid(request)
                .then(Mono.defer(() -> manageService.updateClipboard(request)))
                .then(Mono.just(CommonResponse.success("更新成功")));

//        return manageService.updateClipboard(request)
//                .then(Mono.just(CommonResponse.success("更新成功")));
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
                return Mono.just(ClipUtils.getClipBean());
            } catch (IOException e) {
                return Mono.error(e);
            }
        });
    }

    private static boolean isPull(String operator) {
        return null == operator || OPERATE_PULL.equalsIgnoreCase(operator);
    }

}
