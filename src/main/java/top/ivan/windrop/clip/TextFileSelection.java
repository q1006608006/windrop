package top.ivan.windrop.clip;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * @author Ivan
 * @since 2021/07/20 17:10
 */
public class TextFileSelection extends StringSelection {
    private final List<DataFlavor> flavors = new ArrayList<>();
    private final DataFlavor[] type = new DataFlavor[0];

    private final String text;
    private final Function<String, File> toFile;

    public TextFileSelection(String text, Function<String, File> toFile) {
        super(text);
        if (null != toFile) {
            flavors.add(DataFlavor.javaFileListFlavor);
        }
        flavors.addAll(Arrays.asList(super.getTransferDataFlavors()));
        this.text = text;
        this.toFile = toFile;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return flavors.toArray(type);
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavors.contains(flavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (isDataFlavorSupported(flavor)) {
            if (DataFlavor.javaFileListFlavor.equals(flavor)) {
                File f = toFile.apply(text);
                if (null == f) {
                    throw new IOException("can not support file for text: " + (text.length() > 64 ? text.substring(0, 64) + "..." : text));
                }
                return Collections.singletonList(f);
            }
            return super.getTransferData(flavor);
        }
        throw new UnsupportedFlavorException(flavor);
    }
}
