package top.ivan.windrop.clip;

import net.coobird.thumbnailator.Thumbnails;
import sun.awt.image.MultiResolutionCachedImage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class ImageClipBean implements ClipBean {

    private byte[] data;

    private Image image;

    private long updateTime;

    private String format;

    ImageClipBean() {
    }

    public ImageClipBean(byte[] data, String format, long updateTime) throws IOException {
        this.data = data;
        this.format = format;
        this.updateTime = updateTime;
    }

    public ImageClipBean(Image image, String format, long updateTime) {
        this.image = image;
        this.format = format;
        this.updateTime = updateTime;
    }

    @Override
    public synchronized byte[] getBytes() throws IOException {
        if (data == null) {
            Image img = getImage();
            data = getImageBytes(convert2Buffered(img), format);
        }
        return data;
    }

    private static BufferedImage convert2Buffered(Image img) {
        if(img instanceof BufferedImage) {
            return (BufferedImage) img;
        }
        BufferedImage buffered = new BufferedImage(img.getWidth(null),img.getHeight(null),BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = buffered.createGraphics();
        g.drawImage(img,0,0,null);
        g.dispose();
        return buffered;
    }

    @Override
    public long getUpdateTime() {
        return updateTime;
    }

    @Override
    public Transferable toTransferable() throws IOException {
        return new ImageSelection(getImage());
    }

    @Override
    public boolean isOrigin(Object target) throws IOException {
        if (target instanceof BufferedImage) {
            return Arrays.equals(getBytes(), getImageBytes((BufferedImage) target, this.format));
        }
        return false;
    }

    private static byte[] getImageBytes(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        Thumbnails.of(image).size(image.getWidth(), image.getHeight()).outputFormat(format).toOutputStream(bout);
        return bout.toByteArray();
    }

    public Image getImage() throws IOException {
        if (image == null) {
            this.image = ImageIO.read(new ByteArrayInputStream(data));
        }
        return image;
    }

    @Override
    public String toString() {
        return "@image";
    }
}
