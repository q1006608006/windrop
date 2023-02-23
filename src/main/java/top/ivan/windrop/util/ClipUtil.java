package top.ivan.windrop.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import top.ivan.windrop.clip.*;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static top.ivan.windrop.clip.ClipType.*;

/**
 * @author Ivan
 * @description
 * @date 2020/12/14
 */
public class ClipUtil {
    private ClipUtil() {
    }

    private static final String DEFAULT_IMAGE_FORMAT = "png";

    private static final ClipTypeSelector TYPE_SELECTOR = new ClipTypeSelector(Arrays.asList(F, I, S, U));

    private static final Clipboard CLIPBOARD = Toolkit.getDefaultToolkit().getSystemClipboard();

    private static volatile ClipBean TARGET;

    public static synchronized ClipBean getClipBean() {
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
        } catch (Exception e) {
            throw new RuntimeException("can not get clipboard data", e);
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
            case "file":
                return "文件";
            case "image":
                return "图片";
            case "text":
                return "文本";
            default:
                return "未知类型";
        }
    }

    public static String getClipBeanType(ClipBean clipBean) {
        if (clipBean instanceof FileClipBean) {
            return "file";
        } else if (clipBean instanceof ImageClipBean) {
            return "image";
        } else if (clipBean instanceof TextClipBean) {
            return "text";
        }
        return "unknown";
    }

}
