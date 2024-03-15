package top.ivan.windrop.system.clipboard;

import net.coobird.thumbnailator.Thumbnails;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.Collections;

/**
 * @author Ivan
 * @description
 * @date 2021/2/2
 */
public class ImageFileSelection implements Transferable {

    private static final DataFlavor[] dataFlavors = new DataFlavor[]{DataFlavor.imageFlavor, DataFlavor.javaFileListFlavor};
    private final File file;

    public ImageFileSelection(File file) {
        this.file = file;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return dataFlavors;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor == DataFlavor.imageFlavor || flavor == DataFlavor.javaFileListFlavor;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (flavor == DataFlavor.imageFlavor) {
            return Thumbnails.of(file).scale(1).asBufferedImage();
        } else if (flavor == DataFlavor.javaFileListFlavor) {
            return Collections.singletonList(file);
        }
        throw new UnsupportedFlavorException(flavor);
    }
}
