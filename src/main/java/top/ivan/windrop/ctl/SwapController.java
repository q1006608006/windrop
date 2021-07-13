package top.ivan.windrop.ctl;

import com.alibaba.fastjson.JSON;
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
import reactor.core.publisher.Mono;
import top.ivan.windrop.WinDropApplication;
import top.ivan.windrop.WinDropConfiguration;
import top.ivan.windrop.bean.*;
import top.ivan.windrop.clip.*;
import top.ivan.windrop.ex.HttpClientException;
import top.ivan.windrop.ex.HttpServerException;
import top.ivan.windrop.ex.LengthTooLargeException;
import top.ivan.windrop.svc.PersistUserService;
import top.ivan.windrop.svc.ResourceSharedService;
import top.ivan.windrop.svc.ValidService;
import top.ivan.windrop.util.ClipUtil;
import top.ivan.windrop.util.ConvertUtil;
import top.ivan.windrop.util.IDUtil;
import top.ivan.windrop.verify.VerifyIP;
import top.ivan.windrop.verify.WebHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * @author Ivan
 * @description windrop核心控制器
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

    /**
     * 核心配置
     */
    @Autowired
    private WindropConfig config;

    /**
     * 用户服务
     */
    @Autowired
    private PersistUserService userService;

    /**
     * 资源共享服务
     */
    @Autowired
    private ResourceSharedService sharedService;

    @Autowired
    private ValidService validService;

    /**
     * 定时清理临时文件
     *
     * @throws IOException
     */
    @Scheduled(cron = "* 0/15 * * * ?")
    public void cleanTempFile() throws IOException {
        log.debug("start clean old file...");
        File[] files = TEMP_DIRECTORY_FILE.listFiles();
        if (null == files) {
            return;
        }
        // 遍历缓存路径所有文件
        for (File file : files) {
            // 保留与当前剪贴板一致的文件
            if (!ClipUtil.isOrigin(file)) {
                log.info("clear expired file: '{}'", file);
                file.delete();
            }
        }
    }

    /**
     * 交换请求
     *
     * @param mono 推送数据{@link ApplyRequest}
     * @return 随机访问密钥 {@link ApplyResponse}
     */
    @PostMapping("apply")
    @VerifyIP
    public Mono<ApplyResponse> apply(@RequestBody Mono<ApplyRequest> mono) {
        return mono.flatMap(request -> {
            // 判断请求类型
            boolean isPush = !"pull".equalsIgnoreCase(request.getType());
            // 判断申请的资源类型
            String itemType = isPush ? request.getType() : ClipUtil.getClipBeanType(ClipUtil.getClipBean());

            AccessUser user = prepareUser(request.getId());

            return WebHandler.ip()
                    // PC上确认是否接收
                    .doOnNext(ip -> confirm(user, request, ip, itemType, isPush))
                    // 返回随机密钥,用于签名
                    .map(v -> ApplyResponse.success(validService.getValidKey(getSwapGroupKey(itemType, user, isPush), 90)));
        });
    }

    /**
     * 远端推送请求
     *
     * @param mono 推送数据{@link WindropRequest}
     * @return 更新结果
     */
    @PostMapping("push")
    @VerifyIP
    public Mono<CommonResponse> setClipboard(@RequestBody Mono<WindropRequest> mono) {
        return mono.flatMap(request -> {
            // 校验data
            byte[] data;
            if (null == request.getData()) {
                log.error("request without 'data'");
                throw new HttpClientException(HttpStatus.BAD_REQUEST, "异常请求");
            } else {
                // 使用base64解码
                data = ConvertUtil.decodeBase64(request.getData());
            }

            // 获取用户（设备）信息
            AccessUser user = prepareUser(request.getId());

            // 验证签名
            return validPushRequest(user, request, data)
                    // 更新剪贴板内容
                    .map(r -> set2Clipboard(request, data, user))
                    .flatMap(bean ->
                            // 返回成功结果
                            Mono.just(CommonResponse.success("更新成功"))
                                    // 调用系统通知
                                    .doOnSuccess(rsp -> systemNotify(request.getType(), user, bean, true))
                    );
        });
    }

    /**
     * 远端拉取请求
     *
     * @param mono 拉取请求数据{@link WindropRequest}
     * @return 本地剪切板内容或大文件resourceId
     */
    @PostMapping("pull")
    @VerifyIP
    public Mono<WindropResponse> getClipboard(@RequestBody Mono<WindropRequest> mono) {
        return mono.flatMap(request -> {
            // 用户（设备）信息
            AccessUser user = prepareUser(request.getId());

            // 获取剪贴板信息
            ClipBean clipBean = ClipUtil.getClipBean();


            // 1.验证签名
            return validPullRequest(user, request, ClipUtil.getClipBeanType(clipBean))
                    // 2.封装剪切板内容
                    .map(r -> prepareWindropResponse(clipBean, user))
                    // 2.当数据文件过大返回转浏览器下载协议
                    .onErrorResume(LengthTooLargeException.class, e -> WebHandler.ip().flatMap(ip -> Mono.just(prepareRedirectResponse(clipBean, user, ip))))
                    // 请求完成后发起系统通知
                    .doOnSuccess(s -> systemNotify(s.getType(), user, clipBean, false));
        });
    }

    /**
     * push类型核验，格式: sha256("sha256(data);随机密钥;用户密钥")
     *
     * @param user    用户信息
     * @param request push请求
     * @param data    push的数据（减少base64解码次数）
     */
    private Mono<Boolean> validPushRequest(AccessUser user, WindropRequest request, byte[] data) {
        String group = getSwapGroupKey(request.getType(), user, true);
        String dataSha = DigestUtils.sha256Hex(data);
        return validService.valid(group, request.getSign(), user.getValidKey(), dataSha)
                .doOnNext(success -> {
                    if (!success) throw new HttpClientException(HttpStatus.FORBIDDEN, "核验失败，请重新登陆");
                });
    }

    /**
     * pull类型核验，格式: sha256("随机密钥;用户密钥")
     *
     * @param user       用户信息
     * @param request    pull请求
     * @param targetType 目标类型
     */
    private Mono<Boolean> validPullRequest(AccessUser user, WindropRequest request, String targetType) {
        String group = getSwapGroupKey(targetType, user, false);
        return validService.valid(group, request.getSign(), user.getValidKey())
                .doOnNext(success -> {
                    if (!success) throw new HttpClientException(HttpStatus.FORBIDDEN, "核验失败，请重新登陆");
                });
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

    /**
     * 更新剪贴板
     *
     * @param request push请求
     * @param data    push数据
     */
    private ClipBean set2Clipboard(WindropRequest request, byte[] data, AccessUser user) {
        try {
            ClipBean result;
            switch (request.getType()) {
                case "file":
                    // 同步文件
                    result = setFile2Clipboard(request, data);
                    break;
                case "text":
                    // 同步文本
                    result = setText2Clipboard(request, data);
                    break;
                case "image":
                    // 同步图片
                    result = setImage2Clipboard(request, data);
                    break;
                default:
                    // 不支持的类型
                    log.warn("un support type: {}", request.getType());
                    throw new HttpClientException(HttpStatus.BAD_REQUEST, "不支持的操作");
            }
            log.info("receive {} from [{}]: {}", request.getType(), user.getAlias(), result);
            return result;
        } catch (Exception e) {
            log.error("更新剪贴板失败", e);
            throw new HttpClientException(HttpStatus.BAD_REQUEST, "服务异常");
        }
    }

    /**
     * 创建临时文件
     *
     * @param data   文件数据
     * @param name   文件名称
     * @param suffix 文件后缀名
     * @return 临时文件信息
     * @throws IOException io异常
     */
    private File createTempFile(byte[] data, String name, String suffix) throws IOException {
        File file = new File(TEMP_DIRECTORY_FILE, name + suffix);
        // 应用离开后删除文件
        file.deleteOnExit();
        // NIO方式写入
        Files.write(Paths.get(file.getAbsolutePath()), data);

        log.debug("create temp file: {}", file.getAbsoluteFile());
        return file;
    }

    /**
     * 同步文件
     *
     * @param clipboardData push请求信息
     * @param data          文件数据
     * @return 临时文件信息
     * @throws IOException io异常
     */
    private FileClipBean setFile2Clipboard(WindropRequest clipboardData, byte[] data) throws IOException {
        // 获取文件信息
        String name = formatName(clipboardData.getFilename());
        String suffix = StringUtils.isEmpty(clipboardData.getFileSuffix()) ? "" : "." + clipboardData.getFileSuffix();

        // 创建临时文件
        File tempFile = createTempFile(data, name, suffix);
        FileClipBean bean = new FileClipBean(tempFile, clipboardData.getClientUpdateTime());
        // 同步到剪贴板
        ClipUtil.setClipboard(bean);
        return bean;
    }

    /**
     * 同步图片
     *
     * @param clipboardData push请求信息
     * @param data          图片数据
     * @return 临时文件信息（使用文件方式储存，便于在不同应用中显示图片）
     * @throws IOException io异常
     */
    private ImageFileClipBean setImage2Clipboard(WindropRequest clipboardData, byte[] data) throws IOException {
        // 获取图片文件信息
        String name = formatName(clipboardData.getFilename());
        String suffix = "." + (StringUtils.isEmpty(clipboardData.getFileSuffix()) ? getImageType(data) : clipboardData.getFileSuffix());

        // 创建临时文件
        File imageFile = createTempFile(data, name, suffix);

        ImageFileClipBean bean = new ImageFileClipBean(imageFile, clipboardData.getClientUpdateTime());
        // 同步图片文件到剪贴板
        ClipUtil.setClipboard(bean);
        return bean;
    }

    /**
     * 同步文本
     *
     * @param clipboardData push请求信息
     * @param data          文本数据
     * @return 文本内容
     * @throws IOException io异常
     */
    private TextClipBean setText2Clipboard(WindropRequest clipboardData, byte[] data) throws IOException {
        String text = new String(data, config.getCharset());
        TextClipBean bean = new TextClipBean(text, clipboardData.getClientUpdateTime());
        ClipUtil.setClipboard(bean);
        return bean;
    }

    /**
     * 提炼剪贴板内容
     *
     * @param clipBean 剪贴板内容信息
     * @param user     操作用户（设备）
     * @return 用于返回的剪贴板数据 {@link WindropResponse}
     */
    private WindropResponse prepareWindropResponse(ClipBean clipBean, AccessUser user) {
        // 获取类型
        String type = ClipUtil.getClipBeanType(clipBean);

        byte[] srcData;
        String fileName = null;

        try {
            if (clipBean instanceof TextClipBean) {
                // 获取文本数据
                srcData = ((TextClipBean) clipBean).getBytes(config.getCharset());
            } else if (clipBean instanceof FileClipBean) {
                // 判断文件大小
                if (((FileClipBean) clipBean).getLength() > config.getMaxFileLength()) {
                    throw new LengthTooLargeException();
                }
                // 获取文件数据
                fileName = ((FileClipBean) clipBean).getFileName();
                srcData = clipBean.getBytes();
            } else if (clipBean instanceof ImageClipBean) {
                // 判断图片大小
                if (clipBean.getBytes().length > config.getMaxFileLength()) {
                    throw new LengthTooLargeException();
                }
                // 获取图片数据
                srcData = clipBean.getBytes();
            } else {
                // 其他类型
                throw new HttpServerException(HttpStatus.INTERNAL_SERVER_ERROR, "不支持windrop的类型");
            }
        } catch (IOException e) {
            log.error("获取剪贴板内容失败", e);
            throw new HttpServerException(HttpStatus.INTERNAL_SERVER_ERROR, "获取剪贴板内容失败: " + e.getMessage());
        }

        // 使用base64编码数据
        String data = ConvertUtil.encodeBase64(srcData);
        // 获取文件签名
        String dataSha = DigestUtils.sha256Hex(srcData);
        // 配合用户验证密钥签名
        String sign = DigestUtils.sha256Hex(dataSha + ";" + user.getValidKey());

        WindropResponse response = new WindropResponse();
        response.setData(data);
        response.setType(type);
        response.setFilename(fileName);
        response.setServerUpdateTime(clipBean.getUpdateTime());
        response.setSign(sign);

        log.info("sync {}[{}] to [{}]: {}", type, srcData.length, user.getAlias(), clipBean);
        return response;
    }

    /**
     * 对大文件重定向请求
     *
     * @param clipBean 原始剪贴板内容
     * @param user     用户（设备）信息
     * @return 重定向响应
     */
    private WindropResponse prepareRedirectResponse(ClipBean clipBean, AccessUser user, String ip) {
        // 生成资源id
        String resourceId = IDUtil.getShortUuid();

        // 注册到资源共享服务的路径，通过sha256("resourceId;用户密钥")生成
        String registerKey = DigestUtils.sha256Hex(String.join(";", resourceId, ip, user.getValidKey()));
        if (clipBean instanceof FileBean) {
            // 注册文件类型资源
            sharedService.register(registerKey, ((FileBean) clipBean).getFile(), 1, 180);
        } else {
            // 注册二进制类型资源
            sharedService.register(registerKey, () -> {
                try {
                    return new ByteArrayResource(clipBean.getBytes());
                } catch (IOException ioException) {
                    throw new HttpServerException(HttpStatus.INTERNAL_SERVER_ERROR, "加载数据异常");
                }
            }, 1, 180);
        }

        // 封装请求
        WindropResponse resp = new WindropResponse();
        resp.setServerUpdateTime(clipBean.getUpdateTime());
        resp.setResourceId(resourceId);
        resp.setSuccess(false);
        resp.setType(ClipUtil.getClipBeanType(clipBean));
        resp.setMessage("Request Moved");

        log.info("sync file/octet-stream to [{}] with id: {}", user.getAlias(), resourceId);
        return resp;
    }

    private void systemNotify(String type, AccessUser user, ClipBean clipBean, boolean isPush) {
        if (config.needNotify(type, isPush)) {
            StringBuilder mbd = new StringBuilder();
            if (isPush) {
                mbd.append("收到来自[").append(user.getAlias()).append("]的 ").append(ClipUtil.getClipBeanTypeName(clipBean)).append(" ：\n");
            } else {
                mbd.append("同步 ").append(ClipUtil.getClipBeanTypeName(clipBean)).append(" 到设备[").append(user.getAlias()).append("]：\n");
            }
            if (clipBean instanceof FileBean) {
                mbd.append(((FileBean) clipBean).getFileName());
            } else if (clipBean instanceof TextClipBean) {
                mbd.append("\"").append(((TextClipBean) clipBean).getText()).append("\"");
            } else {
                mbd.delete(mbd.length() - 2, mbd.length() - 1);
            }
            WinDropApplication.WindropHandler.getSystemTray().showNotification(mbd.toString());
        }
    }

    private void confirm(AccessUser user, ApplyRequest request, String ip, String itemType, boolean isPush) {
        if (config.needConfirm(itemType, isPush)) {
            String msg;
            if (!isPush) {
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
                    case "image":
                        msg = "是否接收来自[" + user.getAlias() + "]的文件/图片: " + request.getFilename() + "（" + request.getSize() + ")?";
                        break;
                    case "text":
                        msg = "是否接收来自[" + user.getAlias() + "]的文本?";
                        break;
                    default:
                        msg = "未定义请求: " + JSON.toJSONString(request);
                        break;
                }
            }
            if (!WinDropApplication.WindropHandler.confirm("来自" + ip, msg)) {
                log.info("canceled {} request from {}({})", isPush ? "push" : "pull", user.getAlias(), ip);
                throw new HttpClientException(HttpStatus.FORBIDDEN, "请求已被取消");
            }
        }
    }

    /**
     * 获取交换随机密钥
     */
    private String getSwapGroupKey(String itemType, AccessUser user, boolean isPush) {
        String type = isPush ? "push" : "pull";
        return ConvertUtil.combines("_", WinDropConfiguration.SWAP_GROUP, user.getId(), type, itemType);
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
