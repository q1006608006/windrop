package top.ivan.windrop.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import top.ivan.windrop.WinDropConfiguration;
import top.ivan.windrop.clip.*;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static top.ivan.windrop.clip.ClipType.*;

/**
 * @author Ivan
 * @description
 * @date 2020/12/14
 */
@Slf4j
public class ClipUtil {

    public static final File TEMP_DIRECTORY_FILE;

    public static final String CLIP_TYPE_FILE = "file";
    public static final String CLIP_TYPE_IMAGE = "image";
    public static final String CLIP_TYPE_TEXT = "text";
    public static final String CLIP_TYPE_UNKNOWN = "unknown";

    static {
        //创建临时文件夹
        File tempDir = new File(WinDropConfiguration.TEMP_FILE_PATH);
        if (!tempDir.exists() || !tempDir.isDirectory()) {
            tempDir.mkdir();
        }
        TEMP_DIRECTORY_FILE = tempDir;
    }

    private ClipUtil() {
    }

    private static final String DEFAULT_IMAGE_FORMAT = "png";

    private static final ClipTypeSelector TYPE_SELECTOR = new ClipTypeSelector(Arrays.asList(F, I, S, U));

    private static final Clipboard CLIPBOARD = Toolkit.getDefaultToolkit().getSystemClipboard();

    private static volatile ClipBean TARGET;

    public static void cleanTemp() throws IOException {

    }

    public static synchronized ClipBean getClipBean() throws IOException {
        try {
            Transferable ts = CLIPBOARD.getContents(null);
            Object origin;
            switch (TYPE_SELECTOR.select(ts)) {
                case STRING:
                    origin = ts.getTransferData(DataFlavor.stringFlavor);
                    if (!isOrigin(origin)) {
                        TARGET = new TextClipBean((String) origin, currentTimestamp());
                    }
                    break;
                case IMAGE:
                    ImageData data = getImageData(ts);
                    origin = data.getOrigin();
                    String type = data.getType();

                    if (!isOrigin(origin)) {
                        if (origin instanceof File) {
                            TARGET = new ImageFileClipBean((File) origin, currentTimestamp());
                        } else {
                            TARGET = new ImageClipBean((Image) origin, type, currentTimestamp());
                        }
                    }
                    break;
                case FILE:
                    List<File> fileList = (List<File>) ts.getTransferData(DataFlavor.javaFileListFlavor);
                    origin = fileList.get(0);
                    if (!isOrigin(origin)) {
                        TARGET = new FileClipBean((File) origin, currentTimestamp());
                    }
                    break;
            }
        } catch (UnsupportedFlavorException e) {
            throw new RuntimeException(e);
        }
        return TARGET;
    }

    public static synchronized void setClipboard(ClipBean bean) throws IOException {
        TARGET = bean;
        CLIPBOARD.setContents(bean.toTransferable(), null);
    }

    public static boolean isOrigin(Object obj) throws IOException {
        if (TARGET != null) {
            return TARGET.isOrigin(obj);
        }
        return false;
    }

    private static String imageType(String suffix) {
        if (null == suffix) {
            return DEFAULT_IMAGE_FORMAT;
        }
        if (suffix.contains("gif")) {
            return "gif";
        } else if (suffix.contains("jpg") || suffix.contains("jpeg")) {
            return "jpg";
        }

        return DEFAULT_IMAGE_FORMAT;
    }

    private static ImageData getImageData(Transferable ts) throws IOException, UnsupportedFlavorException {
        String suffix = DEFAULT_IMAGE_FORMAT;
        Object origin;
        if (ts.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            List<File> files = (List<File>) ts.getTransferData(DataFlavor.javaFileListFlavor);
            File f = files.get(0);
            int pos = f.getName().indexOf(".");
            if (pos > 0) {
                suffix = imageType(f.getName().substring(pos + 1));
            }
            return new ImageData(suffix, f);
        }
        if (ts.isDataFlavorSupported(DataFlavor.selectionHtmlFlavor)) {
            String htmlContent = (String) ts.getTransferData(DataFlavor.selectionHtmlFlavor);
            String imageStr = htmlContent.replace("\n", "").replaceAll(".*<img src=\"(.*?)\".*/><.*", "$1");
            int pos = imageStr.lastIndexOf("/");
            if (pos > -1) {
                imageStr = imageStr.substring(pos);
                pos = imageStr.lastIndexOf(".");
                if (pos > -1) {
                    imageStr = imageStr.substring(pos);
                    suffix = imageType(imageStr);
                }
            }
        }
        origin = ts.getTransferData(DataFlavor.imageFlavor);
        return new ImageData(suffix, origin);
    }

    @Data
    @AllArgsConstructor
    private static class ImageData {
        private String type;
        private Object origin;
    }

    private static long currentTimestamp() {
        return System.currentTimeMillis() / 1000;
    }

    public static String getClipBeanTypeName(ClipBean clipBean) {
        switch (getClipBeanType(clipBean)) {
            case CLIP_TYPE_FILE:
                return "文件";
            case CLIP_TYPE_IMAGE:
                return "图片";
            case CLIP_TYPE_TEXT:
                return "文本";
            default:
                return "未知类型";
        }
    }

    public static boolean isFile(ClipBean bean) {
        return bean instanceof FileBean;
    }

    public static boolean isImage(ClipBean bean) {
        return bean instanceof ImageClipBean;
    }

    public static boolean isText(ClipBean bean) {
        return bean instanceof TextClipBean;
    }

    public static String getClipBeanType(ClipBean clipBean) {
        if (isFile(clipBean)) {
            return CLIP_TYPE_FILE;
        } else if (isImage(clipBean)) {
            return CLIP_TYPE_IMAGE;
        } else if (isText(clipBean)) {
            return CLIP_TYPE_TEXT;
        }
        return CLIP_TYPE_UNKNOWN;
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
    public static File createTempFile(byte[] data, String name, String suffix) throws IOException {
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

    /**
     * 同步文件
     *
     * @param clipboardData push请求信息
     * @param data          文件数据
     * @return 临时文件信息
     * @throws IOException io异常
     */
    public static FileClipBean setFile2Clipboard(String filename, String suffix, byte[] data) throws IOException {
        // 创建临时文件
        File tempFile = createTempFile(data, filename, suffix);
        FileClipBean bean = new FileClipBean(tempFile, System.currentTimeMillis());
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
    public static ImageFileClipBean setImage2Clipboard(String filename, String suffix, byte[] data) throws IOException {
        // 创建临时文件
        File imageFile = createTempFile(data, filename, suffix);

        ImageFileClipBean bean = new ImageFileClipBean(imageFile, System.currentTimeMillis());
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
    public static TextClipBean setText2Clipboard(String filename, String suffix, byte[] data, Charset cs) throws IOException {
        String text = new String(data, cs);
        Function<String, File> supply;

        if (!StringUtils.hasLength(suffix)) {
            suffix = "txt";
        }
        // 将超过长度限制的文本或非txt类型的文本保存为文件
        if (null != filename && !"txt".equals(suffix)) {
            File textFile = createTempFile(data, filename, suffix);
            supply = s -> textFile;
        } else {
            supply = null;
        }
        TextClipBean bean = new TextClipBean(text, System.currentTimeMillis(), supply);
        ClipUtil.setClipboard(bean);
        return bean;
    }

}
