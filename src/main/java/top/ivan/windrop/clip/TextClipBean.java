package top.ivan.windrop.clip;

import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

public class TextClipBean implements ClipBean {
    private final String text;
    private final long updateTime;
    private Function<String, File> toFile;

    public TextClipBean(String text, long updateTime) {
        this.text = text;
        this.updateTime = updateTime;
    }

    public TextClipBean(String text, long updateTime, Function<String, File> toFile) {
        this.text = text;
        this.updateTime = updateTime;
        this.toFile = toFile;
    }


    @Override
    public byte[] getBytes() {
        return getBytes(StandardCharsets.UTF_8);
    }

    public byte[] getBytes(Charset charset) {
        return text.getBytes(charset);
    }

    @Override
    public Transferable toTransferable() {
        return new TextFileSelection(text, toFile);
    }

    @Override
    public long getUpdateTime() {
        return updateTime;
    }

    @Override
    public boolean isOrigin(Object target) throws IOException {
        return this.text.equals(target);
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return text;
    }
}
