package top.ivan.windrop.svc;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import top.ivan.windrop.WinDropApplication;
import top.ivan.windrop.WinDropConfiguration;
import top.ivan.windrop.bean.AccessUser;
import top.ivan.windrop.common.ResourceType;
import top.ivan.windrop.exception.HttpClientException;
import top.ivan.windrop.exception.HttpServerException;
import top.ivan.windrop.security.domain.UserAuthEntity;
import top.ivan.windrop.util.ConvertUtil;
import top.ivan.windrop.verify.WebHandler;

import java.io.IOException;
import java.util.function.Function;

/**
 * @author Ivan
 * @since 2023/08/25 20:27
 */
@Service
@Slf4j
public class SecurityManager {

    @Autowired
    private UserService userService;

    @Autowired
    private ValidService validService;

    public Mono<String> buildValidKey(UserAuthEntity auth) {
        return checkEntity(auth)
                .flatMap(this::doConfirm)
                .then(takeValidKey(auth).transform(m -> subscribeUser(auth, m)));
    }

    public Mono<Void> valid(UserAuthEntity auth) {
        return checkEntity(auth)
                .flatMap(this::doValid)
                .transform(m -> subscribeUser(auth, m));
    }

    private Mono<UserAuthEntity> checkEntity(UserAuthEntity auth) {
        if (!auth.isPull() && ResourceType.UNKNOWN == auth.getType()) {
            return HttpClientException.badRequest("未知类型");
        }
        if (!StringUtils.hasText(auth.getId())) {
            return HttpClientException.forbidden("未知来源");
        }
        return Mono.just(auth);
    }

    private Mono<String> takeValidKey(UserAuthEntity auth) {
        return Mono.just(getSwapGroupKey(auth))
                .map(group -> validService.getValidKey(group, 90));
    }

    private Mono<Void> doValid(UserAuthEntity auth) {
        return deferUser(auth)
                .flatMap(user -> {
                    String group = getSwapGroupKey(auth);
                    String sha256 = auth.isPull() ? null : DigestUtils.sha256Hex(auth.toBytes());
                    return WebHandler.ip()
                            .map(ip -> validService.validSign(group, auth.getSign(), ip, user.getValidKey(), sha256))
                            .flatMap(pass -> pass
                                    ? Mono.empty()
                                    : Mono.error(() -> new HttpClientException(HttpStatus.FORBIDDEN, "核验失败，请重新登陆")));
                });
    }

    private Mono<Void> doConfirm(UserAuthEntity auth) {
        boolean isNeedConfirm = true; //todo

        return WebHandler.ip()
                .map(ip -> "来自" + ip)
                .flatMap(title -> buildConfirmMsg(auth)
                        .map(msg -> WinDropApplication.confirm(title, msg))
                        .flatMap(enable -> enable
                                ? Mono.empty()
                                : WebHandler.ip().flatMap(ip -> deferUser(auth)
                                .doOnNext(user ->
                                        log.info("canceled {} request from {}({})"
                                                , auth.getOperate()
                                                , user.getAlias()
                                                , ip
                                        )).then(HttpClientException.forbidden("请求已被取消")))));
    }

    private Mono<String> buildConfirmMsg(UserAuthEntity auth) {
        return deferUser(auth)
                .flatMap(user -> {
                    String title;
                    if (auth.isPull()) {
                        switch (auth.getType()) {
                            case FILE:
                                title = "是否接收来自[" + user.getAlias() + "]的文件: " + auth.getShowName() + "?";
                                break;
                            case IMAGE:
                                title = "是否接收来自[" + user.getAlias() + "]的图片: " + auth.getShowName() + ")?";
                                break;
                            case TEXT:
                                title = "是否接收来自[" + user.getAlias() + "]的文本?";
                                break;
                            default:
                                return HttpClientException.badRequest("未知类型");
                        }
                    } else {
                        title = "是否推送'" + auth.getShowName() + "'到[" + user.getAlias() + "]?";
                    }
                    return Mono.just(title);
                });
    }

    private <T> Mono<T> subscribeUser(UserAuthEntity auth, Mono<T> m) {
        return deferUser(auth, user -> m.contextWrite(
                Context.of(AccessUser.class, user)
        ));
    }

    private Mono<AccessUser> deferUser(UserAuthEntity auth) {
        return Mono.deferContextual(view -> view.hasKey(AccessUser.class)
                ? Mono.just(view.get(AccessUser.class))
                : prepareUser(auth.getId())
        );
    }

    private <T> Mono<T> deferUser(UserAuthEntity auth, Function<AccessUser, Mono<T>> u) {
        return deferUser(auth).flatMap(u);
    }

    private Mono<AccessUser> prepareUser(String id) {
        return Mono.defer(() -> {
            try {
                return Mono.justOrEmpty(userService.findUser(id))
                        .switchIfEmpty(Mono.error(() -> new HttpClientException(HttpStatus.UNAUTHORIZED, "未验证过的设备(id: " + id + ")")))
                        .flatMap(user -> user.isExpired() ? Mono.error(() -> new HttpClientException(HttpStatus.UNAUTHORIZED, "使用许可已过期")) : Mono.just(user));
            } catch (IOException e) {
                log.error("加载用户数据失败", e);
                return Mono.error(new HttpServerException("无法加载用户数据"));
            }
        });
    }

    /**
     * 获取验证组
     *
     * @return 验证组名
     */
    private static String getSwapGroupKey(UserAuthEntity auth) {
        return ConvertUtil.combines("_"
                , WinDropConfiguration.SWAP_GROUP
                , auth.getId()
                , auth.getOperate()
                , auth.getType()
        );
    }


}
