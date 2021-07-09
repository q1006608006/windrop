package top.ivan.windrop.clip;

import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class TextClipBean implements ClipBean {
    private final String text;
    private final long updateTime;


    public TextClipBean(String text, long updateTime) {
        this.text = text;
        this.updateTime = updateTime;
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
        return new StringSelection(text);
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
