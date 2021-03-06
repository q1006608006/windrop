package top.ivan.windrop.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import top.ivan.windrop.bean.AccessUser;
import top.ivan.windrop.clip.*;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ivan
 * @description
 * @date 2020/12/14
 */
public class ClipUtil {
    private ClipUtil() {
    }

    private static final String DEFAULT_IMAGE_FORMAT = "png";

    private static Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

    private static volatile ClipBean target;

    public static synchronized ClipBean getClipBean() {
        try {
            Transferable ts = clipboard.getContents(null);
            Object origin;
            if (ts.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                origin = ts.getTransferData(DataFlavor.stringFlavor);
                if (!isOrigin(origin)) {
                    target = new TextClipBean((String) origin, currentTimestamp());
                }
            } else if (ts.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                ImageData data = getImageData(ts);
                origin = data.getOrigin();
                String type = data.getType();

                if (!isOrigin(origin)) {
                    if (origin instanceof File) {
                        target = new ImageFileClipBean((File) origin, currentTimestamp());
                    } else {
                        target = new ImageClipBean((BufferedImage) origin, type, currentTimestamp());
                    }
                }
            } else if (ts.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                List<File> fileList = (List<File>) ts.getTransferData(DataFlavor.javaFileListFlavor);
                origin = fileList.get(0);
                if (!isOrigin(origin)) {
                    target = new FileClipBean((File) origin, currentTimestamp());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("can not get clipboard data", e);
        }
        return target;
    }

    public static synchronized void setClipboard(ClipBean bean) throws IOException {
        target = bean;
        clipboard.setContents(bean.toTransferable(), null);
    }

    public static boolean isOrigin(Object obj) throws IOException {
        if (target != null) {
            return target.isOrigin(obj);
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
                return "??????";
            case "image":
                return "??????";
            case "text":
                return "??????";
            default:
                return "????????????";
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
