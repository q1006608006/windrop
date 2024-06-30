package top.ivan.jdrop.framework.system.io.clipboard.transfer;

import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.util.List;

public class FileSelection extends DataSelection {
    public FileSelection(List<File> files) {
        super(multiSetFlavors(files, DataFlavor.javaFileListFlavor));
    }
}
