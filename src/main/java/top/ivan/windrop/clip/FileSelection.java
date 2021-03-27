package top.ivan.windrop.clip;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Ivan
 * @description
 * @date 2020/12/17
 */
public class FileSelection implements Transferable {
    private final List<File> files;

    public FileSelection(List<File> files) {
        this.files = files;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{DataFlavor.javaFileListFlavor};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor == DataFlavor.javaFileListFlavor;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (isDataFlavorSupported(flavor)) {
            return files;
        }
        throw new UnsupportedFlavorException(flavor);
    }
}
