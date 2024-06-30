package top.ivan.jdrop.framework.system.io.clipboard.transfer;

import java.awt.datatransfer.DataFlavor;

public class UrlSelection extends DataSelection {

    public UrlSelection(String url) {
        super(multiSetFlavors(url, DataFlavor.stringFlavor, DataFlavor.allHtmlFlavor));
    }
}
