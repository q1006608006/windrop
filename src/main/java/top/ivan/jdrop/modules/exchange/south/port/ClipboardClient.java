package top.ivan.jdrop.modules.exchange.south.port;

import top.ivan.windrop.system.clipboard.ClipType;

import java.io.File;
import java.util.List;

public interface ClipboardClient {

    void setText(String text);

    void setFiles(List<File> file);

    void setImageFiles(List<File> images);

    ClipType getClipType();

    <T> T getClipboardData(ClipType type);

}
