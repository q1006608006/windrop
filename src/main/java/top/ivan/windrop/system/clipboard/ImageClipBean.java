package top.ivan.windrop.system.clipboard;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.lang.NonNull;

import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageClipBean implements ClipBean {

    private byte[] data;

    private Image image;

    private long updateTime;

    private String format;

    ImageClipBean() {
    }

    public ImageClipBean(@NonNull Image image, String format, long updateTime) {
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
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        BufferedImage buffered = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = buffered.createGraphics();
        g.drawImage(img, 0, 0, null);
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
        //always need to load image-bytes, so just return false is better
        return false;
    }

    private static byte[] getImageBytes(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        Thumbnails.of(image).size(image.getWidth(), image.getHeight()).outputFormat(format).toOutputStream(bout);
        return bout.toByteArray();
    }

    public Image getImage() throws IOException {
        return image;
    }

    @Override
    public String toString() {
        return "@image";
    }
}
