package top.ivan.jdrop.framework.system.io.clipboard;

import top.ivan.jdrop.framework.system.io.FileFactory;
import top.ivan.jdrop.framework.util.IDUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TransferImageUtils {

    public static File webImageToFile(Transferable ts, FileFactory factory) throws IOException, UnsupportedFlavorException {
        BufferedImage img = transferToImage(ts);
        String imgUrl = parseImageURL(ts);
        String name = parseResourceName(imgUrl);
        String type = getImageType(name);
        return writeImage(factory, img, name, type);
    }

    private static File writeImage(FileFactory factory, BufferedImage img, String name, String type) throws IOException {
        if (!name.endsWith(type)) {
            name = name + "." + type;
        }

        File file = factory.createFile(formatFilename(name));
        ImageIO.write(img, type, file);
        return file;
    }

    public static File imageToFile(Transferable ts, FileFactory factory) throws IOException, UnsupportedFlavorException {
        BufferedImage img = transferToImage(ts);
        String type = "png";
        String name = IDUtils.get32UUID();
        return writeImage(factory, img, name, type);
    }

    public static BufferedImage transferToImage(Transferable ts) throws IOException, UnsupportedFlavorException {
        Image img = (Image) ts.getTransferData(DataFlavor.imageFlavor);
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        } else {
            BufferedImage buffered = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = buffered.createGraphics();
            g.drawImage(img, 0, 0, null);
            g.dispose();
            return buffered;
        }
    }

    private static String parseImageURL(Transferable ts) {
        String htmlContent = null;
        try {
            htmlContent = (String) ts.getTransferData(DataFlavor.selectionHtmlFlavor);
        } catch (UnsupportedFlavorException | IOException e) {
            throw new RuntimeException(e);
        }
        return htmlContent.replace("\n", "").replaceAll(".*<img src=\"(.*?)\".*/><.*", "$1");
    }


    private static String getImageType(String name) {
        if (name != null) {
            if (name.endsWith(".png")) {
                return "png";
            } else if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
                return "jpg";
            } else if (name.endsWith(".gif")) {
                return "gif";
            } else if (name.endsWith(".webp")) {
                return "webp";
            }
        }
        //png for default
        return "png";
    }

    public static final Pattern NAME_PATTERN = Pattern.compile("([^:/?@#&=]*\\.(png|jpeg|jpg|gif|webp))[^A-z]?");
    public static final Pattern RESOURCE_PATTERN = Pattern.compile("/([^/?]+)[^/]+$");

    private static String parseResourceName(String imageUrl) {
        Matcher matcher = NAME_PATTERN.matcher(imageUrl);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            matcher = RESOURCE_PATTERN.matcher(imageUrl);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return imageUrl;
    }

    private static String formatFilename(String fn) {
        return fn.replaceAll("[\\\\/:*?\"<>|]", "");
    }

}
