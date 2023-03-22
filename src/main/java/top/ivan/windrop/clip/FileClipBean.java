package top.ivan.windrop.clip;

import lombok.extern.slf4j.Slf4j;

import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

@Slf4j
public class FileClipBean implements ClipBean, FileBean {
    private byte[] data;
    private final File src;
    private final long updateTime;

    public FileClipBean(File file, long updateTime) {
        this.src = file;
        this.updateTime = updateTime;
    }

    @Override
    public synchronized byte[] getBytes() throws IOException {
        if (data == null) {
            if (src.isDirectory()) {
                throw new UnsupportedOperationException("un support covert a directory to byte arrays, use covert2Zip() instead it");
            } else {
                this.data = Files.readAllBytes(Paths.get(src.toURI()));
            }
        }
        return data;
    }

    @Override
    public Transferable toTransferable() {
        return new FileSelection(Collections.singletonList(src));
    }

    @Override
    public long getUpdateTime() {
        return updateTime;
    }

    @Override
    public boolean isOrigin(Object target) {
        return src.equals(target);
    }

    @Override
    public String getFileName() {
        return src.getName();
    }

    @Override
    public File getFile() {
        return src;
    }

    @Override
    public String toString() {
        return getFile().getAbsolutePath();
    }


}
