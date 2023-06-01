package top.ivan.windrop.clip;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Ivan
 * @description
 * @date 2020/12/17
 */
public class FileSelection implements Transferable {
    private final Map<DataFlavor, Object> flavorMap;

    public FileSelection(List<File> files) {
        flavorMap = new HashMap<>();
        flavorMap.put(DataFlavor.javaFileListFlavor, files);
        flavorMap.put(DataFlavor.stringFlavor, files.stream().map(File::getName).collect(Collectors.joining(", ")));
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{DataFlavor.javaFileListFlavor, DataFlavor.stringFlavor};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavorMap.containsKey(flavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (!isDataFlavorSupported(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }
        return flavorMap.get(flavor);
    }
}
