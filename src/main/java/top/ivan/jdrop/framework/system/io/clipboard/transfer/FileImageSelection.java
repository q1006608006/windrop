package top.ivan.jdrop.framework.system.io.clipboard.transfer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class FileImageSelection extends DataSelection {
    public FileImageSelection(List<File> files) {
        super(initFlavorsMap(files));
    }

    private static Map<DataFlavor, Object> initFlavorsMap(List<File> files) {
        Map<DataFlavor, Object> map = new HashMap<>();
        map.put(DataFlavor.javaFileListFlavor, files);
        map.put(DataFlavor.imageFlavor, loadImageFromFile(files.get(0)));
        return map;
    }

    private static Supplier<Image> loadImageFromFile(File f) {
        return () -> {
            try {
                return ImageIO.read(f);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

}
