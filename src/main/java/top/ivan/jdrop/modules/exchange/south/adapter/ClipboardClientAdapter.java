package top.ivan.jdrop.modules.exchange.south.adapter;

import org.springframework.stereotype.Component;
import top.ivan.jdrop.modules.exchange.south.port.ClipboardClient;
import top.ivan.windrop.system.clipboard.ClipType;

import java.io.File;
import java.util.List;

@Component
public class ClipboardClientAdapter implements ClipboardClient {

    @Override
    public void setText(String text) {

    }

    @Override
    public void setFiles(List<File> file) {

    }

    @Override
    public void setImageFiles(List<File> images) {

    }

    @Override
    public ClipType getClipType() {
        return null;
    }

    @Override
    public <T> T getClipboardData(ClipType type) {
        return null;
    }
}
