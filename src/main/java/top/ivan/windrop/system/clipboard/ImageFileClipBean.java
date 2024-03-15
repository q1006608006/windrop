package top.ivan.windrop.system.clipboard;

import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Ivan
 * @description
 * @date 2021/2/2
 */
public class ImageFileClipBean extends ImageClipBean implements FileBean {

    private byte[] data;
    private final File file;
    private final long updateTime;

    public ImageFileClipBean(File file, long updateTime) {
        this.file = file;
        this.updateTime = updateTime;
    }

    @Override
    public synchronized byte[] getBytes() throws IOException {
        if (data == null) {
            data = Files.readAllBytes(Paths.get(file.toURI()));
        }
        return data;
    }

    @Override
    public long getUpdateTime() {
        return updateTime;
    }

    @Override
    public Transferable toTransferable() throws IOException {
        return new ImageFileSelection(file);
    }

    @Override
    public boolean isOrigin(Object target) throws IOException {
        return file.equals(target);
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public String getFileName() {
        return file.getName();
    }

    @Override
    public String toString() {
        return file.getAbsolutePath();
    }
}
