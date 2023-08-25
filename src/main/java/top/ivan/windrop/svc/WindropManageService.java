package top.ivan.windrop.svc;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.util.context.Context;
import reactor.util.context.ContextView;
import top.ivan.windrop.WinDropApplication;
import top.ivan.windrop.WinDropConfiguration;
import top.ivan.windrop.bean.*;
import top.ivan.windrop.clip.ClipBean;
import top.ivan.windrop.clip.FileBean;
import top.ivan.windrop.clip.FileClipBean;
import top.ivan.windrop.clip.TextClipBean;
import top.ivan.windrop.ex.HttpClientException;
import top.ivan.windrop.ex.HttpServerException;
import top.ivan.windrop.ex.LengthTooLargeException;
import top.ivan.windrop.util.*;
import top.ivan.windrop.verify.WebHandler;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * @author Ivan
 * @since 2023/06/30 11:13
 */

@Service
@Slf4j
public class WindropManageService {

    /**
     * 核心配置
     */
    @Autowired
    private WindropConfig config;

    private static final String OPERATE_PUSH = "PUSH";
    private static final String OPERATE_PULL = "PULL";
    private static final Pattern URL_PATTERN = Pattern.compile("(((http|ftp|https)://)(([a-zA-Z0-9._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*)(/[a-zA-Z0-9,=&%_./-~-#]*)?");
    public static final String RES_TYPE_FILE = "文件";
    public static final String RES_TYPE_URL = "网址";
    private static final String KEY_FOR_REQUEST_NEED_VALID = "@REQUEST_NEED_VALID@";
    private static final String KEY_FOR_DISABLE_TRANS2REDIRECT = "@DISABLE_TRANS2REDIRECT@";

    @Autowired
    private UserService userService;

    @Autowired
    private ValidService validService;

    /**
     * 资源共享服务
     */
    @Autowired
    private ResourceSharedService sharedService;

    @Resource(name = "asyncExecutor")
    private AsyncTaskExecutor asyncExecutor;

    /**
     * 定时清理临时文件
     *
     * @throws IOException
     */
    @Scheduled(cron = "0 0/15 * * * ?")
    public void cleanTempFile() throws IOException {
        log.debug("start clean old file...");
        File[] files = ClipUtil.TEMP_DIRECTORY_FILE.listFiles();
        if (null == files) {
            return;
        }
        // 遍历缓存路径所有文件
        for (File file : files) {
            // 保留与当前剪贴板一致的文件
            if (!ClipUtil.isOrigin(file)) {
                log.info("clear unless file: '{}'", file);
                Files.delete(Paths.get(file.toURI()));
            }
        }
    }

    public Mono<String> takeValidKey(ApplyRequest req, ClipBean bean) {
        boolean isPull = isPull(req.getType());
        return Mono.just(isPull ? ClipUtil.getClipBeanType(bean) : req.getType())
                .flatMap(itemType -> confirm(bean, itemType, isPull)
                        .then(Mono.fromSupplier(() -> validService.getValidKey(getSwapGroupKey(itemType, req.getId(), isPull), 90))))
                .transform(m -> prepareContext(m, req.getId(), Context.of(ApplyRequest.class, req)));
    }

    public VerifierMono<Void> updateClipboard(WindropRequest request) {
        byte[] data = ConvertUtil.decodeBase64(request.getData());
        Mono<Void> publisher = validRequest(request.getType(), DigestUtils.sha256Hex(data))
                .then(set2Clipboard(data))
                .flatMap(this::handle)
                .then()
                .transform(m -> prepareContext(m, request.getId(), Context.of(WindropRequest.class, request)));
        return new VerifierMono<>(publisher);
    }

    public VerifierMono<WindropResponse> quickResponse(WindropRequest req, ClipBean bean, boolean enableRedirect) {
        Mono<WindropResponse> publisher = validRequest(ClipUtil.getClipBeanType(bean), null)
                .then(handle(bean))
                .then(pocketResponse(bean))
                .onErrorResume(LengthTooLargeException.class,
                        e -> enableRedirect
                                ? redirectResponse(req, bean).withoutValid()
                                : Mono.error(e)
                ).transform(m -> prepareContext(m, req.getId(), Context.of(WindropRequest.class, req)));
        return new VerifierMono<>(publisher);
    }

    public VerifierMono<WindropResponse> redirectResponse(WindropRequest req, ClipBean bean) {
        Mono<WindropResponse> publisher = validRequest(ClipUtil.getClipBeanType(bean), null)
                .then(handle(bean))
                .then(prepareRedirectResponse(bean))
                .transform(m -> prepareContext(m, req.getId(), Context.of(WindropRequest.class, req)));

        return new VerifierMono<>(publisher);
    }

    private <T> Mono<T> prepareContext(Mono<T> mono, String userId, ContextView ctx) {
        return prepareUser(userId)
                .flatMap(user -> mono.contextWrite(Context.of(ctx).put(AccessUser.class, user)));
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

    private Mono<Void> validRequest(String requiredType, String content) {
        return Mono.deferContextual(view -> {
            if (Boolean.FALSE.equals(view.getOrDefault(KEY_FOR_REQUEST_NEED_VALID, true))) {
                return Mono.empty();
            }
            AccessUser user = view.get(AccessUser.class);
            WindropRequest req = view.get(WindropRequest.class);
            String group = getSwapGroupKey(requiredType, req.getId(), isPull(req.getType()));

            return WebHandler.ip()
                    .map(ip -> validService.validSign(group, req.getSign(), ip, user.getValidKey(), content))
                    .flatMap(pass -> pass
                            ? Mono.empty()
                            : Mono.error(() -> new HttpClientException(HttpStatus.FORBIDDEN, "核验失败，请重新登陆")));
        });
    }

    private Mono<ClipBean> handle(ClipBean bean) {
        return Mono.deferContextual(view -> Mono.just(bean).doFinally(s -> {
            WindropRequest request = view.get(WindropRequest.class);
            AccessUser user = view.get(AccessUser.class);
            boolean isPull = isPull(request.getType());
            if (SignalType.ON_COMPLETE == s) {
                systemNotify(request.getType(), user, bean, isPull);
                if (!isPull) {
                    tryOpen(bean, request.getType());
                }
            }
        }));
    }

    /**
     * 更新剪贴板
     *
     * @param data push数据
     */
    private Mono<ClipBean> set2Clipboard(byte[] data) {
        return Mono.deferContextual(view -> {
            try {
                WindropRequest request = view.get(WindropRequest.class);
                ClipBean result;
                switch (request.getType()) {
                    case "file":
                        // 同步文件
                        result = ClipUtil.setFile2Clipboard(request.getFilename(), request.getFileSuffix(), data);
                        break;
                    case "text":
                        // 同步文本
                        result = ClipUtil.setText2Clipboard(request.getFilename(), request.getFileSuffix(), data, config.getCharset());
                        break;
                    case "image":
                        // 同步图片
                        result = ClipUtil.setImage2Clipboard(request.getFilename(), request.getFileSuffix(), data);
                        break;
                    default:
                        // 不支持的类型
                        log.warn("un support type: {}", request.getType());
                        return Mono.error(new HttpClientException(HttpStatus.BAD_REQUEST, "不支持的操作"));
                }

                AccessUser user = view.get(AccessUser.class);
                log.info("receive {} from [{}]: {}", request.getType(), user.getAlias(), ConvertUtil.limitStringSize(result.toString(), 256));

                return Mono.just(result);
            } catch (Exception e) {
                log.error("更新剪贴板失败", e);
                return Mono.error(new HttpClientException(HttpStatus.BAD_REQUEST, "服务异常"));
            }
        });
    }

    /**
     * 提炼剪贴板内容
     *
     * @param bean 剪贴板内容
     * @return 用于返回的剪贴板数据 {@link WindropResponse}
     */
    private Mono<WindropResponse> pocketResponse(ClipBean bean) {
        return pocket(bean).flatMap(data -> {
            WindropResponse response = new WindropResponse();
            response.setData(ConvertUtil.encodeBase64(data));
            String type = ClipUtil.getClipBeanType(bean);
            response.setType(type);
            if (ClipUtil.isFile(bean)) {
                response.setFilename(((FileBean) bean).getFileName());
            }
            response.setServerUpdateTime(bean.getUpdateTime());
            return getUserFromCtx().doOnNext(user -> response.setSign(
                            DigestUtils.sha256Hex(DigestUtils.sha256Hex(data) + ";" + user.getValidKey())
                    )).doOnNext(user -> log.info("sync {}[{}] to [{}]: {}", type, data.length, user.getAlias(), bean))
                    .thenReturn(response);
        });
    }

    private Mono<byte[]> pocket(ClipBean bean) {
        Function<ClipBean, Mono<byte[]>> takeClipData = b -> Mono.defer(() -> {
            try {
                return Mono.just(b.getBytes());
            } catch (IOException e) {
                log.error("获取剪贴板内容失败", e);
                return Mono.error(
                        new HttpServerException(HttpStatus.INTERNAL_SERVER_ERROR, "获取剪贴板内容失败: " + e.getMessage())
                );
            }
        });

        String type = ClipUtil.getClipBeanType(bean);
        switch (type) {
            case ClipUtil.CLIP_TYPE_FILE:
                FileBean fb = (FileBean) bean;
                File src = fb.getFile();
                if (FileUtil.getFilesLength(src) > config.getMaxFileLength()) {
                    return Mono.error(new LengthTooLargeException());
                }
                Mono<ClipBean> beanMono;
                if (src.isDirectory()) {
                    beanMono = Mono.fromSupplier(() -> new FileClipBean(
                            ConvertUtil.covertDir2Zip(src, getTempPath(ConvertUtil.getZipName(src)), null, null)
                            , System.currentTimeMillis()
                    ));
                } else {
                    beanMono = Mono.just(bean);
                }
                return beanMono.flatMap(takeClipData);
            case ClipUtil.CLIP_TYPE_IMAGE:
                return takeClipData.apply(bean).flatMap(data -> data.length > config.getMaxFileLength()
                        ? Mono.error(new LengthTooLargeException())
                        : Mono.just(data));
            case ClipUtil.CLIP_TYPE_TEXT:
                return takeClipData.apply(bean);
            default:
                return Mono.error(new HttpServerException(HttpStatus.INTERNAL_SERVER_ERROR, "windrop不支持的类型"));
        }
    }

    /**
     * 对大文件重定向请求
     *
     * @param clipBean 原始剪贴板内容
     * @return 重定向响应
     */
    public Mono<WindropResponse> prepareRedirectResponse(ClipBean clipBean) {
        String resourceId = IDUtil.getShortUuid();

        // 注册到资源共享服务的路径，通过sha256("resourceId;用户密钥")生成
        Mono<String> keyGenerator = getUserFromCtx().flatMap(user -> WebHandler.ip()
                .map(ip -> DigestUtils.sha256Hex(String.join(";", resourceId, ip, user.getValidKey())))
        );

        Mono<?> register;
        if (ClipUtil.isFile(clipBean)) {
            if (((FileBean) clipBean).getFile().isDirectory()) {
                FileBean fb = (FileBean) clipBean;
                InitialResourceWrapper delay = new InitialResourceWrapper();
                File zipFile = ConvertUtil.covertDir2Zip(
                        fb.getFile()
                        , getTempPath(ConvertUtil.getZipName(fb.getFile()))
                        , delay
                        , asyncExecutor
                );
                clipBean = new FileClipBean(zipFile);
                register = keyGenerator.doOnNext(key ->
                        sharedService.register(key, delay, 1, 180)
                );
            } else {
                register = Mono.just(clipBean).cast(FileClipBean.class).flatMap(bean ->
                        keyGenerator.doOnNext(key ->
                                sharedService.register(key, bean.getFile(), 1, 180)
                        ));
            }
        } else {
            register = Mono.just(clipBean).flatMap(bean -> {
                try {
                    return Mono.just(new ByteArrayResource(bean.getBytes()));
                } catch (IOException e) {
                    return Mono.error(new HttpServerException(HttpStatus.INTERNAL_SERVER_ERROR, "加载数据异常"));
                }
            }).flatMap(res -> keyGenerator.doOnNext(key ->
                    sharedService.register(key, res, 1, 180)
            ));
        }

        return register.then(Mono.just(clipBean).flatMap(bean -> {
            WindropResponse resp = new WindropResponse();
            resp.setServerUpdateTime(bean.getUpdateTime());
            resp.setResourceId(resourceId);
            resp.setSuccess(false);
            resp.setType(ClipUtil.getClipBeanType(bean));
            resp.setMessage("redirected");

            return getUserFromCtx().doOnSuccess(user ->
                    log.info("sync file/octet-stream to [{}] with id: {}", user.getAlias(), resourceId)
            ).thenReturn(resp);
        }));
    }


    private void systemNotify(String type, AccessUser user, ClipBean clipBean, boolean isPull) {
        // 判断是否需要调用系统提醒
//        if (needNotify(type, isPush)) { todo
        if (true) {
            StringBuilder mbd = new StringBuilder();
            if (isPull) {
                mbd.append("同步 ").append(ClipUtil.getClipBeanTypeName(clipBean)).append(" 到设备[").append(user.getAlias()).append("]：\n");
            } else {
                mbd.append("收到来自[").append(user.getAlias()).append("]的 ").append(ClipUtil.getClipBeanTypeName(clipBean)).append(" ：\n");
            }
            if (clipBean instanceof FileBean) {
                mbd.append(((FileBean) clipBean).getFileName());
            } else if (clipBean instanceof TextClipBean) {
                mbd.append("\"").append(((TextClipBean) clipBean).getText()).append("\"");
            } else {
                mbd.delete(mbd.length() - 2, mbd.length() - 1);
            }
            // 调用系统提醒
            WinDropApplication.showNotification(mbd.toString());
        }
    }

    private void tryOpen(ClipBean bean, String type) {
        String resName;
        String resType;
        if (bean instanceof FileBean) {
            resName = ((FileBean) bean).getFileName();
            resType = RES_TYPE_FILE;
        } else if (bean instanceof TextClipBean) {
            String txt = ((TextClipBean) bean).getText();
            if (StringUtils.hasLength(txt) && URL_PATTERN.matcher(txt).matches()) {
                resName = txt;
                resType = RES_TYPE_URL;
            } else {
                return;
            }
        } else {
            return;
        }

        if (WinDropApplication.confirm("ip", "是否打开" + type + ": " + ConvertUtil.shortName(resName, resType.equals(RES_TYPE_URL) ? 50 : -50) + "?")) {
            if (RES_TYPE_FILE.equals(resType)) {
                WinDropApplication.open(((FileBean) bean).getFile());
            } else {
                WinDropApplication.openInBrowse(resName);
            }
        }
    }


    private Mono<Void> confirm(ClipBean bean, String itemType, boolean isPull) {
        // 无需弹窗确认则直接返回
        if (!needConfirm(itemType, isPull)) {
            return Mono.empty();
        }
        return Mono.deferContextual(view -> {
            AccessUser user = view.get(AccessUser.class);
            ApplyRequest request = view.get(ApplyRequest.class);
            String msg;
            if (isPull) {
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
                        msg = "是否接收来自[" + user.getAlias() + "]的文件: " + ConvertUtil.shortName(request.getFilename(), -50) + "（" + request.getSize() + ")?";
                        break;
                    case "image":
                        msg = "是否接收来自[" + user.getAlias() + "]的图片: " + ConvertUtil.shortName(request.getFilename(), -50) + "（" + request.getSize() + ")?";
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
            return WebHandler.ip().flatMap(ip ->
                    Mono.just(WinDropApplication.confirm("来自" + ip, msg))
                            .flatMap(pass -> pass
                                    ? Mono.empty()
                                    : Mono.fromRunnable(() -> log.info("canceled {} request from {}({})", isPull ? "push" : "pull", user.getAlias(), ip))
                                    .then(Mono.error(() -> new HttpClientException(HttpStatus.FORBIDDEN, "请求已被取消")))
                            ));
        });
    }

    private boolean needConfirm(String itemType, boolean isPull) {
        return true; //todo
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

    private static boolean isPull(String operator) {
        return null == operator || OPERATE_PULL.equalsIgnoreCase(operator);
    }

    private static String getTempPath(String filename) {
        return new File(ClipUtil.TEMP_DIRECTORY_FILE, filename).getAbsolutePath();
    }

    private static Mono<AccessUser> getUserFromCtx() {
        return Mono.deferContextual(view -> Mono.just(view.get(AccessUser.class)));
    }

    public static class VerifierMono<T> extends Mono<T> {
        private final Publisher<T> publisher;

        public VerifierMono(Publisher<T> publisher) {
            this.publisher = publisher;
        }

        @Override
        public void subscribe(CoreSubscriber<? super T> coreSubscriber) {
            publisher.subscribe(coreSubscriber);
        }

        public Mono<T> withoutValid() {
            return this.contextWrite(Context.of(KEY_FOR_REQUEST_NEED_VALID, false));
        }

    }

}
