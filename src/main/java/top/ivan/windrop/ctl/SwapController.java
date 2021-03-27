package top.ivan.windrop.ctl;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import top.ivan.windrop.ex.HttpClientException;
import top.ivan.windrop.WinDropApplication;
import top.ivan.windrop.bean.*;
import top.ivan.windrop.clip.*;
import top.ivan.windrop.svc.ClipSvc;
import top.ivan.windrop.svc.IPVerifier;
import top.ivan.windrop.svc.PersistUserService;
import top.ivan.windrop.svc.ValidKeyService;
import top.ivan.windrop.util.ConvertUtil;
import top.ivan.windrop.util.RandomAccessKey;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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
        File tempDir = new File("temp");
        if (!tempDir.exists() || !tempDir.isDirectory()) {
            tempDir.mkdir();
        }
        TEMP_DIRECTORY_FILE = tempDir;
    }

    @Autowired
    private ClipSvc clipSvc;

    @Autowired
    private ValidKeyService validKeyService;

    @Autowired
    private WindropConfig config;

    @Autowired
    private IPVerifier ipVerifier;

    @Autowired
    private PersistUserService userService;

    private final RandomAccessKey randomAccessKey = new RandomAccessKey(30);

    @Scheduled(cron = "* 0/15 * * * ?")
    public void cleanTempFile() throws IOException {
        log.debug("start clean old file...");
        File[] files = TEMP_DIRECTORY_FILE.listFiles();
        if (null == files) {
            return;
        }
        for (File file : files) {
            if (!clipSvc.isOrigin(file)) {
                log.info("clear expired file: '{}'", file);
                file.delete();
            }
        }
    }

    @PostMapping("apply")
    public ResponseEntity<?> apply(@RequestBody ApplyRequest request, ServerWebExchange exchange) throws IOException {
        InetSocketAddress address = exchange.getRequest().getRemoteAddress();
        log.debug("receive apply request from '{}'", exchange.getRequest().getRemoteAddress());

        String ip = address.getAddress().getHostAddress();
        if (!ipVerifier.accessible(ip)) {
            return failure(HttpStatus.FORBIDDEN, "未授予白名单");
        }

        AccessUser user = prepareUser(request.getId());
        if (!confirm(ip, request, user)) {
            return ResponseEntity.ok(ApplyResponse.failed("请求已被取消"));
        }

        String accessKey = randomAccessKey.getAccessKey();
        log.debug("apply for accessKey: {}", accessKey);
        return ResponseEntity.ok(ApplyResponse.success(accessKey));
    }

    @PostMapping("push")
    public ResponseEntity<?> setClipboard(@RequestBody WindropRequest request, ServerWebExchange exchange) throws IOException {
        log.info("receive push request from '{}'", exchange.getRequest().getRemoteAddress());

        byte[] data;
        if (null == request.getData()) {
            log.info("not found 'data'");
            return failure(HttpStatus.BAD_REQUEST, "异常请求");
        } else {
            data = ConvertUtil.decodeBase64(request.getData());
        }

        AccessUser user = prepareUser(request.getId());
        validPushRequest(user, request, data);

        try {
            String msg = "收到来自" + user.getAlias();
            switch (request.getType()) {
                case "file":
                    File file = setFile2Clipboard(request, data);
                    log.info("saved file: '{}', length: {}", file.getAbsolutePath(), file.length());
                    msg += "的文件: " + file.getAbsolutePath();
                    break;

                case "text":
                    String text = setText2Clipboard(request, data);
                    log.info("update clipboard's text" + (config.isEnableShowText() ? ": '" + text + "'" : ""));
                    msg += "的消息" + (config.isEnableShowText() ? ": '" + text + "'" : "");
                    break;

                case "image":
                    file = setImage2Clipboard(request, data);
                    log.info("update clipboard's image; saved image to file: '{}', length: {}", file.getAbsolutePath(), file.length());
                    msg += "的图片: " + file.getAbsolutePath();
                    break;

                default:
                    log.info("un support type: {}", request.getType());
                    return failure(HttpStatus.BAD_REQUEST, "不支持的操作");
            }
            systemNotify(request.getType(), msg, true);
        } catch (Exception e) {
            log.error("更新剪贴板失败", e);
            return failure(HttpStatus.BAD_REQUEST, "服务异常");
        }
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("msg", "更新成功");
        return ResponseEntity.ok(response);
    }

    @PostMapping("pull")
    public ResponseEntity<?> getClipboard(@RequestBody WindropRequest request, ServerWebExchange exchange) throws IOException {
        log.info("receive pull request from '{}'", exchange.getRequest().getRemoteAddress());

        AccessUser user = prepareUser(request.getId());
        validPullRequest(user, request);

        ClipBean clipBean = clipSvc.getClipBean();
        String type;
        byte[] srcData;
        String fileName = null;
        try {
            if (clipBean instanceof TextClipBean) {
                type = "text";
                srcData = ((TextClipBean) clipBean).getBytes(config.getCharset());
            } else if (clipBean instanceof FileClipBean) {
                type = "file";
                if (((FileClipBean) clipBean).getLength() > config.getMaxFileLength()) {
                    return failure(HttpStatus.INTERNAL_SERVER_ERROR, "无法传输超过" + config.getMaxFileLength() / 1048576 + "MB的文件");
                }
                fileName = ((FileClipBean) clipBean).getFileName();
                srcData = clipBean.getBytes();
            } else if (clipBean instanceof ImageClipBean) {
                if (clipBean.getBytes().length > config.getMaxFileLength()) {
                    return failure(HttpStatus.INTERNAL_SERVER_ERROR, "无法传输超过" + config.getMaxFileLength() / 1048576 + "MB的文件");
                }
                type = "image";
                srcData = clipBean.getBytes();
            } else {
                return failure(HttpStatus.INTERNAL_SERVER_ERROR, "不支持windrop的类型");
            }
        } catch (Exception e) {
            log.error("获取剪贴板内容失败", e);
            return failure(HttpStatus.INTERNAL_SERVER_ERROR, "获取剪贴板内容失败: " + e.getMessage());
        }

        String data = ConvertUtil.encodeBase64(srcData);
        WindropResponse response = new WindropResponse();
        response.setData(data);
        response.setType(type);
        response.setFileName(fileName);
        response.setServerUpdateTime(clipBean.getUpdateTime());
        String dataSha = DigestUtils.sha256Hex(data);
        String sign = DigestUtils.sha256Hex(dataSha + ";" + validKeyService.getValidKey());
        response.setSign(sign);

        systemNotify(type, "", false);
        return ResponseEntity.ok(response);
    }

    private File setFile2Clipboard(WindropRequest clipboardData, byte[] data) throws IOException {
        String name = formatName(clipboardData.getFileName());
        String suffix = StringUtils.isEmpty(clipboardData.getFileSuffix()) ? "" : "." + clipboardData.getFileSuffix();

        File tempFile = createTempFile(data, name, suffix);
        FileClipBean clipBean = new FileClipBean(tempFile, clipboardData.getClientUpdateTime());
        clipSvc.setClipboard(clipBean);
        return tempFile;
    }

    private File setImage2Clipboard(WindropRequest clipboardData, byte[] data) throws IOException {
        String name = formatName(clipboardData.getFileName());
        String suffix = "." + (StringUtils.isEmpty(clipboardData.getFileSuffix()) ? getImageType(data) : clipboardData.getFileSuffix());

        File imageFile = createTempFile(data, name, suffix);
        clipSvc.setClipboard(new ImageFileClipBean(imageFile, clipboardData.getClientUpdateTime()));
        return imageFile;
    }

    private String setText2Clipboard(WindropRequest clipboardData, byte[] data) throws IOException {
        String text = new String(data, config.getCharset());
        clipSvc.setClipboard(new TextClipBean(text, clipboardData.getClientUpdateTime()));
        return text;
    }

    private File createTempFile(byte[] data, String name, String suffix) throws IOException {
        File file = new File(TEMP_DIRECTORY_FILE, name + suffix);
        Files.write(Paths.get(file.getAbsolutePath()), data);
        return file;
    }

    private void systemNotify(String type, String msg, boolean push) {
        if (config.needNotify(type, push)) {
            WinDropApplication.WindropHandler.getSystemTray().showNotification(msg);
        }
    }

    private boolean confirm(String ip, ApplyRequest request, AccessUser user) {
        String type = request.getType();
        if (config.needConfirm(getClipBeanType(), !type.equalsIgnoreCase("pull"))) {
            String msg;
            switch (type) {
                case "pull":
                    msg = "推送'" + getClipBeanTypeName() + "'到设备?";
                    break;
                case "file":
                case "image":
                    msg = "是否接收文件/图片: " + request.getFilename() + "（" + request.getSize() + ")?";
                    break;
                case "text":
                    msg = "是否接收文本?";
                    break;
                default:
                    msg = "未定义请求: " + JSON.toJSONString(request);
                    break;
            }
            return WinDropApplication.WindropHandler.confirm("来自" + user.getAlias() + "(" + ip + ")", msg);
        }
        return true;
    }

    private String getClipBeanTypeName() {
        switch (getClipBeanType()) {
            case "file":
                return "文件";
            case "image":
                return "图片";
            case "text":
                return "文本";
        }
        return "未知类型";
    }

    private String getClipBeanType() {
        ClipBean clipBean = clipSvc.getClipBean();
        if (clipBean instanceof FileClipBean) {
            return "file";
        } else if (clipBean instanceof ImageClipBean) {
            return "image";
        } else if (clipBean instanceof TextClipBean) {
            return "text";
        }
        return "unknown";
    }

    private void validPullRequest(AccessUser user, WindropRequest request) {
        if (!randomAccessKey.match(key -> Objects.equals(DigestUtils.sha256Hex(key + ";" + user.getValidKey()), request.getSign()), true)) {
            throw new HttpClientException(HttpStatus.FORBIDDEN, "权限校验失败");
        }
    }

    private void validPushRequest(AccessUser user, WindropRequest request, byte[] data) {
        if (!randomAccessKey.match(key -> Objects.equals(DigestUtils.sha256Hex(DigestUtils.sha256Hex(data) + ";" + key + ";" + user.getValidKey()), request.getSign()), true)) {
            throw new HttpClientException(HttpStatus.FORBIDDEN, "权限校验失败");
        }
    }

    private AccessUser prepareUser(String id) throws IOException {
        AccessUser user = userService.findUser(id);
        if (null == user) {
            throw new HttpClientException(HttpStatus.UNAUTHORIZED, "未验证的设备(id: " + id + ")");
        }
        return user;
    }

    private static String formatName(String src) {
        String name = StringUtils.isEmpty(src) ? "copyfile" + System.currentTimeMillis() / 1000 : src;
        name = name.replaceAll("[\\\\/:*?\"<>|]", "");
        if (name.length() > 61) {
            return name.substring(0, 61);
        }
        return name;
    }

    private ResponseEntity<Map<String, Object>> failure(HttpStatus status, String msg) {
        Map<String, Object> data = new HashMap<>();
        data.put("success", false);
        data.put("message", msg);
        return ResponseEntity.status(status).body(data);
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
