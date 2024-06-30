package top.ivan.jdrop.framework.system.io.clipboard.transfer;

import java.awt.datatransfer.DataFlavor;

public class TextSelection extends DataSelection {
    public TextSelection(String text) {
        super(multiSetFlavors(text, DataFlavor.stringFlavor));
    }
}
