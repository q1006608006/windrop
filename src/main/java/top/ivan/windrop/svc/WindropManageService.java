package top.ivan.windrop.svc;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import top.ivan.windrop.WinDropApplication;
import top.ivan.windrop.WinDropConfiguration;
import top.ivan.windrop.bean.AccessUser;
import top.ivan.windrop.bean.ApplyRequest;
import top.ivan.windrop.bean.WindropRequest;
import top.ivan.windrop.clip.ClipBean;
import top.ivan.windrop.clip.FileBean;
import top.ivan.windrop.ex.HttpClientException;
import top.ivan.windrop.ex.HttpServerException;
import top.ivan.windrop.util.ClipUtil;
import top.ivan.windrop.util.ConvertUtil;
import top.ivan.windrop.util.JSONUtil;
import top.ivan.windrop.verify.WebHandler;

import java.io.IOException;

/**
 * @author Ivan
 * @since 2023/06/30 11:13
 */

@Service
@Slf4j
public class WindropManageService {
    private static final String OPERATE_PUSH = "PUSH";
    private static final String OPERATE_PULL = "PULL";

    @Autowired
    private UserService userService;

    @Autowired
    private ValidService validService;

    public Mono<String> prepareValidKey(ApplyRequest req) {
        boolean isPull = OPERATE_PULL.equalsIgnoreCase(req.getType());
        // 判断申请的资源类型
        String itemType = isPull ? ClipUtil.getClipBeanType(ClipUtil.getClipBean()) : req.getType();

        return prepareUser(req.getId())
                .zipWith(WebHandler.ip(), (user, ip) -> confirm(user, req, ip, itemType, isPull))
                .then(Mono.just(validService.getValidKey(getSwapGroupKey(itemType, req.getId(), isPull), 90)));
    }

    public Mono<AccessUser> validPushForUser(WindropRequest req, byte[] data) {
        String group = getSwapGroupKey(req.getType(), req.getId(), false);
        return prepareUser(req.getId())
                .filterWhen(user -> Mono.fromSupplier(() -> DigestUtils.sha256Hex(data))
                        .flatMap(hex -> validService.valid(group, req.getSign(), user.getValidKey(), hex)))
                .switchIfEmpty(Mono.error(new HttpClientException(HttpStatus.FORBIDDEN, "核验失败，请重新登陆")));
    }

    public Mono<Void> handler() {
        return Mono.empty();
    }

    private Mono<AccessUser> prepareUser(String id) {
        return Mono.defer(() -> {
            try {
                return Mono.justOrEmpty(userService.findUser(id))
                        .switchIfEmpty(Mono.error(new HttpClientException(HttpStatus.UNAUTHORIZED, "未验证过的设备(id: " + id + ")")))
                        .flatMap(user -> user.isExpired() ? Mono.error(new HttpClientException(HttpStatus.UNAUTHORIZED, "使用许可已过期")) : Mono.just(user));
            } catch (IOException e) {
                log.error("加载用户数据失败", e);
                return Mono.error(new HttpServerException("无法加载用户数据"));
            }
        });
    }

    private Mono<Void> confirm(AccessUser user, ApplyRequest request, String ip, String itemType, boolean isPull) {
        // 无需弹窗确认则直接返回
        if (!needConfirm(itemType, isPull)) {
            return Mono.empty();
        }
        String msg;
        if (isPull) {
            ClipBean bean = ClipUtil.getClipBean();
            String itemName;
            if (bean instanceof FileBean) {
                itemName = ((FileBean) bean).getFileName();
            } else {
                itemName = ClipUtil.getClipBeanTypeName(bean);
            }
            msg = "是否推送'" + itemName + "'到[" + user.getAlias() + "]?";
        } else {
            switch (itemType) {
                case "file":
                    msg = "是否接收来自[" + user.getAlias() + "]的文件: " + shortName(request.getFilename(), -50) + "（" + request.getSize() + ")?";
                    break;
                case "image":
                    msg = "是否接收来自[" + user.getAlias() + "]的图片: " + shortName(request.getFilename(), -50) + "（" + request.getSize() + ")?";
                    break;
                case "text":
                    msg = "是否接收来自[" + user.getAlias() + "]的文本?";
                    break;
                default:
                    msg = "未定义请求: " + JSONUtil.toString(request);
                    break;
            }
        }

        // 弹窗确认
        if (!WinDropApplication.confirm("来自" + ip, msg)) {
            log.info("canceled {} request from {}({})", isPull ? "push" : "pull", user.getAlias(), ip);
            // 点击取消则拒绝此处请求
            throw new HttpClientException(HttpStatus.FORBIDDEN, "请求已被取消");
        }

        return Mono.empty();
    }

    private boolean needConfirm(String itemType, boolean isPull) {
        return true;
    }


    private static String shortName(String name, int limit) {
        int len = name.length() + limit;
        if (len > name.length() && limit < name.length()) {
            return name.substring(0, limit) + "...";
        } else if (len < name.length() && len > 0) {
            return name.substring(len) + "...";
        } else {
            return name;
        }
    }

    /**
     * 获取验证组
     *
     * @param itemType 操作对象类型
     * @param userId   请求用户ID
     * @param isPull   是否push请求
     * @return 验证组名
     */
    private static String getSwapGroupKey(String itemType, String userId, boolean isPull) {
        String type = isPull ? "pull" : "push";
        return ConvertUtil.combines("_", WinDropConfiguration.SWAP_GROUP, userId, type, itemType);
    }
}
