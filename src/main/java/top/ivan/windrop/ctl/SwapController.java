package top.ivan.windrop.ctl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
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
import top.ivan.windrop.svc.WindropManageService;
import top.ivan.windrop.util.*;
import top.ivan.windrop.verify.VerifyIP;
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
 * @description windrop核心控制器
 * @date 2020/12/17
 */
@Slf4j
@RestController
@RequestMapping("/windrop")
public class SwapController {
    private static final File TEMP_DIRECTORY_FILE;
    private static final Pattern URL_PATTERN = Pattern.compile("(((http|ftp|https)://)(([a-zA-Z0-9._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*)(/[a-zA-Z0-9,=&%_./-~-#]*)?");
    public static final String RES_TYPE_FILE = "文件";
    public static final String RES_TYPE_URL = "网址";


    static {
        //创建临时文件夹
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

    /**
     * 验证器
     */
    @Autowired
    private ValidService validService;

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
        File[] files = TEMP_DIRECTORY_FILE.listFiles();
        if (null == files) {
            return;
        }
        // 遍历缓存路径所有文件
        for (File file : files) {
            // 保留与当前剪贴板一致的文件
            if (!ClipUtil.isOrigin(file)) {
                log.info("clear expired file: '{}'", file);
                Files.delete(Paths.get(file.toURI()));
            }
        }
    }

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
        return manageService.prepareValidKey(request)// todo
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

        return Mono.fromSupplier(() -> ConvertUtil.decodeBase64(request.getData()))
                // 校验用户请求
                .flatMap(data -> manageService.validPushForUser(request, data)
                        // 更新剪贴板内容
                        .zipWhen(user -> Mono.just(set2Clipboard(request, data, user))
                                        .flatMap(bean -> Mono.just(CommonResponse.success("更新成功"))
                                                // 调用系统通知
                                                .doFinally(s -> {
                                                    if (SignalType.ON_COMPLETE == s) {
                                                        systemNotify(request.getType(), user, bean, true);
                                                        tryOpen(bean, request.getType());
                                                    }
                                                }))
                                , (ig, rsp) -> rsp));
    }

    /**
     * 请求服务端剪贴板
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
     * push类型核验
     *
     * @param user    用户信息
     * @param request push请求
     * @param data    push的数据（减少base64解码次数）
     */
    private Mono<Boolean> validPushRequest(AccessUser user, WindropRequest request, byte[] data) {
        String group = getSwapGroupKey(request.getType(), user, true);
        String dataSha = DigestUtils.sha256Hex(data);
        return validService.valid(group, request.getSign(), user.getValidKey(), dataSha)
                .flatMap(result -> Boolean.TRUE.equals(result) ? Mono.just(true) : Mono.error(new HttpClientException(HttpStatus.FORBIDDEN, "核验失败，请重新登陆")));
    }

    /**
     * pull类型核验
     *
     * @param user       用户信息
     * @param request    pull请求
     * @param targetType 目标类型
     */
    private Mono<Boolean> validPullRequest(AccessUser user, WindropRequest request, String targetType) {
        String group = getSwapGroupKey(targetType, user, false);
        return validService.valid(group, request.getSign(), user.getValidKey())
                .flatMap(result -> Boolean.TRUE.equals(result) ? Mono.just(true) : Mono.error(new HttpClientException(HttpStatus.FORBIDDEN, "核验失败，请重新登陆")));
    }

    /**
     * 查找用户
     *
     * @param id 用户ID
     * @return 用户信息
     */
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
            log.info("receive {} from [{}]: {}", request.getType(), user.getAlias(), ConvertUtil.limitStringSize(result.toString(), 256));
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
        name = formatName(name);
        if (!StringUtils.hasLength(suffix)) {
            suffix = "";
        } else {
            suffix = suffix.startsWith(".") ? suffix : "." + suffix;
        }

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
        // 创建临时文件
        File tempFile = createTempFile(data, clipboardData.getFilename(), clipboardData.getFileSuffix());
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
     * @return 临时文件图片
     * @throws IOException io异常
     */
    private ImageFileClipBean setImage2Clipboard(WindropRequest clipboardData, byte[] data) throws IOException {
        // 创建临时文件
        File imageFile = createTempFile(data, clipboardData.getFilename(), clipboardData.getFileSuffix());

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
        Function<String, File> supply;
        String suffix = clipboardData.getFileSuffix();

        if (!StringUtils.hasLength(suffix)) {
            suffix = "txt";
        }
        // 将超过长度限制的文本或非txt类型的文本保存为文件
        if (data.length > config.getTextFileLimit() || !"txt".equals(suffix)) {
            File textFile = createTempFile(data, clipboardData.getFilename(), suffix);
            supply = s -> textFile;
        } else {
            supply = null;
        }
        TextClipBean bean = new TextClipBean(text, System.currentTimeMillis(), supply);
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
            } else if (clipBean instanceof FileBean) {
                FileBean fb = (FileBean) clipBean;
                File src = fb.getFile();
                // 判断文件大小
                if (FileUtil.getFilesLength(src) > config.getMaxFileLength()) {
                    throw new LengthTooLargeException();
                }
                if (src.isDirectory()) {
                    clipBean = covert2Zip(src, getTempPath(getZipName(src)), null, true);
                }
                // 获取文件数据
                fileName = ((FileBean) clipBean).getFileName();
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

        // 封装返回body
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
            if (((FileBean) clipBean).getFile().isDirectory()) {
                FileBean fb = (FileBean) clipBean;
                InitialResourceWrapper delay = new InitialResourceWrapper();
                clipBean = covert2Zip(fb.getFile(), getTempPath(getZipName(fb.getFile())), delay, false);
                sharedService.register(registerKey, delay, 1, 180);
            } else {
                sharedService.register(registerKey, ((FileBean) clipBean).getFile(), 1, 180);
            }
        } else {
            // 注册二进制类型资源
            ClipBean byteBean = clipBean;
            sharedService.register(registerKey, () -> {
                try {
                    return new ByteArrayResource(byteBean.getBytes());
                } catch (IOException ioException) {
                    throw new HttpServerException(HttpStatus.INTERNAL_SERVER_ERROR, "加载数据异常");
                }
            }, 1, 180);
        }

        // 封装返回body
        WindropResponse resp = new WindropResponse();
        resp.setServerUpdateTime(clipBean.getUpdateTime());
        resp.setResourceId(resourceId);
        resp.setSuccess(false);
        resp.setType(ClipUtil.getClipBeanType(clipBean));
        resp.setMessage("Request Moved");

        log.info("sync file/octet-stream to [{}] with id: {}", user.getAlias(), resourceId);
        return resp;
    }

    public FileClipBean covert2Zip(File src, String path, InitialResourceWrapper wrapper, boolean sync) {
        File file = new File(path);
        if (file.isDirectory()) {
            file = new File(file, src.getName() + ".zip");
        }

        File fin = file;
        Runnable run = () -> {
            boolean failed = false;
            if (!fin.exists()) {
                try {
                    ZipUtil.nioZip(src, fin);
                } catch (IOException e) {
                    failed = true;
                    log.error("zip file failed", e);
                }
            }
            log.info("zip '{}' finished", fin);
            if (!failed && wrapper != null) {
                wrapper.complete(fin);
            }
        };
        if (sync) {
            run.run();
        } else {
            asyncExecutor.execute(run);
        }

        return new FileClipBean(fin, System.currentTimeMillis());
    }

    /**
     * 调用系统提醒
     *
     * @param type     操作类型
     * @param user     操作用户
     * @param clipBean 操作对象
     * @param isPush   是否push请求
     */
    private void systemNotify(String type, AccessUser user, ClipBean clipBean, boolean isPush) {
        // 判断是否需要调用系统提醒
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
            // 调用系统提醒
            WinDropApplication.showNotification(mbd.toString());
        }
    }

    /**
     * 弹窗请求确认
     *
     * @param user     请求用户
     * @param request  请求body
     * @param ip       请求来源
     * @param itemType 操作对象类型
     * @param isPush   是否push请求
     */
    private void confirm(AccessUser user, ApplyRequest request, String ip, String itemType, boolean isPush) {
        // 无需弹窗确认则直接返回
        if (!config.needConfirm(itemType, isPush)) {
            return;
        }
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
            log.info("canceled {} request from {}({})", isPush ? "push" : "pull", user.getAlias(), ip);
            // 点击取消则拒绝此处请求
            throw new HttpClientException(HttpStatus.FORBIDDEN, "请求已被取消");
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

        if (WinDropApplication.confirm("ip", "是否打开" + type + ": " + shortName(resName, resType.equals(RES_TYPE_URL) ? 50 : -50) + "?")) {
            if (RES_TYPE_FILE.equals(resType)) {
                WinDropApplication.open(((FileBean) bean).getFile());
            } else {
                WinDropApplication.openInBrowse(resName);
            }
        }
    }

    /**
     * 获取验证组
     *
     * @param itemType 操作对象类型
     * @param user     请求用户
     * @param isPush   是否push请求
     * @return 验证组名
     */
    private String getSwapGroupKey(String itemType, AccessUser user, boolean isPush) {
        String type = isPush ? "push" : "pull";
        return ConvertUtil.combines("_", WinDropConfiguration.SWAP_GROUP, user.getId(), type, itemType);
    }

    /**
     * 格式化文件名（防止恶意字符）
     *
     * @param src 源文件名
     * @return 格式化后的文件名
     */
    private static String formatName(String src) {
        String name = !StringUtils.hasLength(src) ? "copyfile" + System.currentTimeMillis() / 1000 : src;
        name = name.replaceAll("[\\\\/:*?\"<>|]", "");
        return name;
    }

    private static String getTempPath(String filename) {
        return new File(TEMP_DIRECTORY_FILE, filename).getAbsolutePath();
    }

    private static String getZipName(File file) {
        if (file.isDirectory()) {
            return file.getName() + ".zip";
        }
        return file.getName().replaceAll("(.*)(\\..*)?", "$1.zip");
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

    private static class InitialResourceWrapper extends InitialResource {

        private File file;

        public InitialResourceWrapper() {
        }

        @Override
        public boolean isReady() {
            return file != null;
        }

        public void complete(File file) {
            this.file = file;
        }

        @Override
        public org.springframework.core.io.Resource takeResource() {
            return new FileSystemResource(file);
        }
    }
}
