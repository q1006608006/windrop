package top.ivan.windrop.ctl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import top.ivan.windrop.WinDropApplication;
import top.ivan.windrop.WinDropConfiguration;
import top.ivan.windrop.bean.*;
import top.ivan.windrop.clip.*;
import top.ivan.windrop.ex.HttpClientException;
import top.ivan.windrop.ex.HttpServerException;
import top.ivan.windrop.ex.LengthTooLargeException;
import top.ivan.windrop.svc.IPVerifier;
import top.ivan.windrop.svc.PersistUserService;
import top.ivan.windrop.svc.RandomAccessKeyService;
import top.ivan.windrop.svc.ResourceSharedService;
import top.ivan.windrop.util.ClipUtil;
import top.ivan.windrop.util.ConvertUtil;
import top.ivan.windrop.util.IDUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author Ivan
 * @description
 * @date 2020/12/17
 */
@Slf4j
@RestController
@RequestMapping("/windrop")
public class SwapController {
    private static final File TEMP_DIRECTORY_FILE;

    static {
        File tempDir = new File(WinDropConfiguration.TEMP_FILE_PATH);
        if (!tempDir.exists() || !tempDir.isDirectory()) {
            tempDir.mkdir();
        }
        TEMP_DIRECTORY_FILE = tempDir;
    }

    @Autowired
    private WindropConfig config;
    @Autowired
    private PersistUserService userService;
    @Autowired
    private ResourceSharedService sharedService;
    @Autowired
    private IPVerifier ipVerifier;
    @Autowired
    private RandomAccessKeyService keyService;

    @Scheduled(cron = "* 0/15 * * * ?")
    public void cleanTempFile() throws IOException {
        log.debug("start clean old file...");
        File[] files = TEMP_DIRECTORY_FILE.listFiles();
        if (null == files) {
            return;
        }
        for (File file : files) {
            if (!ClipUtil.isOrigin(file)) {
                log.info("clear expired file: '{}'", file);
                file.delete();
            }
        }
    }

    @PostMapping("apply")
    public Mono<ApplyResponse> apply(@RequestBody ApplyRequest request, ServerWebExchange exchange) {
        log.debug("receive apply request from '{}'", exchange.getRequest().getRemoteAddress());

        String ip = Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress();
        boolean isPush = !"pull".equalsIgnoreCase(request.getType());
        String itemType = isPush ? request.getType() : ClipUtil.getClipBeanType(ClipUtil.getClipBean());

        return Mono.fromSupplier(() -> ApplyResponse.success(keyService.getKey(getSwapGroupKey(itemType, isPush))))
                .doOnSubscribe(s -> verifyIp(ip))
                .doOnRequest(s -> confirm(ip, request, itemType, isPush));
    }

    @PostMapping("push")
    public Mono<CommonResponse> setClipboard(@RequestBody WindropRequest request, ServerWebExchange exchange) {
        log.info("receive push request from '{}'", exchange.getRequest().getRemoteAddress());

        byte[] data;
        if (null == request.getData()) {
            log.info("request without 'data'");
            throw new HttpClientException(HttpStatus.BAD_REQUEST, "异常请求");
        } else {
            data = ConvertUtil.decodeBase64(request.getData());
        }
        AccessUser user = prepareUser(request.getId());

        StringBuilder successMsg = new StringBuilder();
        return Mono.fromRunnable(() -> set2Clipboard(request, data, user, successMsg))
                .doOnRequest(req -> validPushRequest(user, request, data))
                .then(Mono.fromSupplier(() -> CommonResponse.success("更新成功")))
                .doOnSuccess(rsp -> systemNotify(request.getType(), successMsg.toString(), true));
    }

    @PostMapping("pull")
    public Mono<WindropResponse> getClipboard(@RequestBody WindropRequest request, ServerWebExchange exchange) {
        log.info("receive pull request from '{}'", exchange.getRequest().getRemoteAddress());

        AccessUser user = prepareUser(request.getId());
        ClipBean clipBean = ClipUtil.getClipBean();

        StringBuilder successMsg = new StringBuilder();
        return Mono.fromSupplier(() -> prepareWindropResponse(clipBean, user, successMsg))
                .onErrorResume(LengthTooLargeException.class, e -> Mono.just(prepareRedirectResponse(clipBean, user, successMsg)))
                .doOnRequest(req -> validPullRequest(user, request, ClipUtil.getClipBeanType(clipBean)))
                .doOnSuccess(s -> systemNotify(s.getType(), successMsg.toString(), false));
    }

    private void validPushRequest(AccessUser user, WindropRequest request, byte[] data) {
        if (!keyService.match(getSwapGroupKey(request.getType(), true), key -> Objects.equals(DigestUtils.sha256Hex(DigestUtils.sha256Hex(data) + ";" + key + ";" + user.getValidKey()), request.getSign()))) {
            throw new HttpClientException(HttpStatus.FORBIDDEN, "权限校验失败");
        }
    }

    private void validPullRequest(AccessUser user, WindropRequest request, String targetType) {
        if (!keyService.match(getSwapGroupKey(targetType, false), key -> Objects.equals(DigestUtils.sha256Hex(key + ";" + user.getValidKey()), request.getSign()))) {
            throw new HttpClientException(HttpStatus.FORBIDDEN, "权限校验失败");
        }
    }

    private AccessUser prepareUser(String id) {
        try {
            AccessUser user = userService.findUser(id);
            if (null == user) {
                throw new HttpClientException(HttpStatus.UNAUTHORIZED, "未验证的设备(id: " + id + ")");
            }
            if (user.isExpired()) {
                throw new HttpClientException(HttpStatus.UNAUTHORIZED, "使用许可已过期");
            }
            return user;
        } catch (IOException e) {
            log.error("加载用户数据失败", e);
            throw new HttpServerException(HttpStatus.INTERNAL_SERVER_ERROR, "数据服务异常");
        }
    }

    private void set2Clipboard(WindropRequest request, byte[] data, AccessUser user, StringBuilder successMsg) {
        try {
            successMsg.append("收到来自").append(user.getAlias());
            switch (request.getType()) {
                case "file":
                    File file = setFile2Clipboard(request, data);
                    log.info("saved file: '{}', length: {}", file.getAbsolutePath(), file.length());
                    successMsg.append("的文件: ").append(file.getAbsolutePath());
                    break;
                case "text":
                    String text = setText2Clipboard(request, data);
                    log.info("update clipboard's text" + (config.isEnableShowText() ? ": '" + text + "'" : ""));
                    successMsg.append("的消息").append(config.isEnableShowText() ? ": '" + text + "'" : "");
                    break;
                case "image":
                    file = setImage2Clipboard(request, data);
                    log.info("update clipboard's image; saved image to file: '{}', length: {}", file.getAbsolutePath(), file.length());
                    successMsg.append("的图片: ").append(file.getAbsolutePath());
                    break;
                default:
                    log.info("un support type: {}", request.getType());
                    throw new HttpClientException(HttpStatus.BAD_REQUEST, "不支持的操作");
            }
        } catch (Exception e) {
            log.error("更新剪贴板失败", e);
            throw new HttpClientException(HttpStatus.BAD_REQUEST, "服务异常");
        }
    }

    private File createTempFile(byte[] data, String name, String suffix) throws IOException {
        File file = new File(TEMP_DIRECTORY_FILE, name + suffix);
        Files.write(Paths.get(file.getAbsolutePath()), data);
        return file;
    }

    private File setFile2Clipboard(WindropRequest clipboardData, byte[] data) throws IOException {
        String name = formatName(clipboardData.getFileName());
        String suffix = StringUtils.isEmpty(clipboardData.getFileSuffix()) ? "" : "." + clipboardData.getFileSuffix();

        File tempFile = createTempFile(data, name, suffix);
        FileClipBean clipBean = new FileClipBean(tempFile, clipboardData.getClientUpdateTime());
        ClipUtil.setClipboard(clipBean);
        return tempFile;
    }

    private File setImage2Clipboard(WindropRequest clipboardData, byte[] data) throws IOException {
        String name = formatName(clipboardData.getFileName());
        String suffix = "." + (StringUtils.isEmpty(clipboardData.getFileSuffix()) ? getImageType(data) : clipboardData.getFileSuffix());

        File imageFile = createTempFile(data, name, suffix);
        ClipUtil.setClipboard(new ImageFileClipBean(imageFile, clipboardData.getClientUpdateTime()));
        return imageFile;
    }

    private String setText2Clipboard(WindropRequest clipboardData, byte[] data) throws IOException {
        String text = new String(data, config.getCharset());
        ClipUtil.setClipboard(new TextClipBean(text, clipboardData.getClientUpdateTime()));
        return text;
    }

    private WindropResponse prepareWindropResponse(ClipBean clipBean, AccessUser user, StringBuilder successMsg) {
        String type = ClipUtil.getClipBeanType(clipBean);

        byte[] srcData;
        String fileName = null;

        try {
            if (clipBean instanceof TextClipBean) {
                srcData = ((TextClipBean) clipBean).getBytes(config.getCharset());
                successMsg.append("发送剪切板文本到设备");
            } else if (clipBean instanceof FileClipBean) {
                if (((FileClipBean) clipBean).getLength() > config.getMaxFileLength()) {
                    throw new LengthTooLargeException();
                }
                fileName = ((FileClipBean) clipBean).getFileName();
                srcData = clipBean.getBytes();
                successMsg.append("发送剪切板文件到设备");
            } else if (clipBean instanceof ImageClipBean) {
                if (clipBean.getBytes().length > config.getMaxFileLength()) {
                    throw new LengthTooLargeException();
                }
                srcData = clipBean.getBytes();
                successMsg.append("发送剪切板图片到设备");
            } else {
                throw new HttpServerException(HttpStatus.INTERNAL_SERVER_ERROR, "不支持windrop的类型");
            }
        } catch (IOException e) {
            log.error("获取剪贴板内容失败", e);
            throw new HttpServerException(HttpStatus.INTERNAL_SERVER_ERROR, "获取剪贴板内容失败: " + e.getMessage());
        }

        String data = ConvertUtil.encodeBase64(srcData);
        WindropResponse response = new WindropResponse();
        response.setData(data);
        response.setType(type);
        response.setFileName(fileName);
        response.setServerUpdateTime(clipBean.getUpdateTime());
        String dataSha = DigestUtils.sha256Hex(data);
        String sign = DigestUtils.sha256Hex(dataSha + ";" + user.getValidKey());
        response.setSign(sign);

        return response;
    }

    private WindropResponse prepareRedirectResponse(ClipBean clipBean, AccessUser user, StringBuilder successMsg) {
        String resourceId = IDUtil.getShortUuid();
        log.info("data is too large, shared with resourceId: {}", resourceId);

        String registerKey = DigestUtils.sha256Hex(resourceId + ";" + user.getValidKey());
        if (clipBean instanceof FileBean) {
            sharedService.register(registerKey, ((FileBean) clipBean).getFile(), 1, 60);
        } else {
            sharedService.register(registerKey, () -> {
                try {
                    return new ByteArrayResource(clipBean.getBytes());
                } catch (IOException ioException) {
                    throw new HttpServerException(HttpStatus.INTERNAL_SERVER_ERROR, "加载数据异常");
                }
            }, 1, 60);
        }

        WindropResponse resp = new WindropResponse();
        resp.setServerUpdateTime(clipBean.getUpdateTime());
        resp.setResourceId(resourceId);
        resp.setSuccess(false);
        resp.setType(ClipUtil.getClipBeanType(clipBean));
        resp.setMessage("Request Moved");

        successMsg.append("发送大文件到设备");

        return resp;
    }

    private void systemNotify(String type, String msg, boolean isPush) {
        if (config.needNotify(type, isPush)) {
            WinDropApplication.WindropHandler.getSystemTray().showNotification(msg);
        }
    }

    private void confirm(String ip, ApplyRequest request, String itemType, boolean isPush) {
        if (config.needConfirm(itemType, isPush)) {
            String msg;
            AccessUser user = prepareUser(request.getId());
            if (!isPush) {
                ClipBean bean = ClipUtil.getClipBean();
                String itemName;
                if (bean instanceof FileBean) {
                    itemName = ((FileBean) bean).getFileName();
                } else {
                    itemName = ClipUtil.getClipBeanTypeName(bean);
                }
                msg = "是否推送'" + itemName + "'到" + user.getAlias() + "?";
            } else {
                switch (itemType) {
                    case "file":
                    case "image":
                        msg = "是否接收来自" + user.getAlias() + "的文件/图片: " + request.getFilename() + "（" + request.getSize() + ")?";
                        break;
                    case "text":
                        msg = "是否接收来自" + user.getAlias() + "的文本?";
                        break;
                    default:
                        msg = "未定义请求: " + JSON.toJSONString(request);
                        break;
                }
            }
            if (!WinDropApplication.WindropHandler.confirm("来自" + ip, msg)) {
                log.debug("canceled the request: {}", JSONObject.toJSONString(request));
                throw new HttpClientException(HttpStatus.FORBIDDEN, "请求已被取消");
            }
        }
    }

    private void verifyIp(String ip) {
        if (!ipVerifier.accessible(ip)) {
            log.info("unavailable ip: {}", ip);
            throw new HttpClientException(HttpStatus.FORBIDDEN, "未授予白名单");
        }
    }

    private String getSwapGroupKey(String itemType, boolean isPush) {
        if (isPush) {
            return String.join("_", WinDropConfiguration.SWAP_GROUP, "push", itemType);
        } else {
            return String.join("_", WinDropConfiguration.SWAP_GROUP, "pull", itemType);
        }
    }

    private static String formatName(String src) {
        String name = StringUtils.isEmpty(src) ? "copyfile" + System.currentTimeMillis() / 1000 : src;
        name = name.replaceAll("[\\\\/:*?\"<>|]", "");
        if (name.length() > 61) {
            return name.substring(0, 61);
        }
        return name;
    }

    public static String getImageType(byte[] is) {
        String type = "png";
        if (is != null) {
            byte[] b = Arrays.copyOf(is, 4);
            String hexStr = new String(Hex.encodeHex(b)).toUpperCase();
            if (hexStr.startsWith("FFD8FF")) {
                type = "jpg";
            } else if (hexStr.startsWith("89504E47")) {
                type = "png";
            } else if (hexStr.startsWith("47494638")) {
                type = "gif";
            } else if (hexStr.startsWith("424D")) {
                type = "bmp";
            }
        }
        return type;
    }

}
