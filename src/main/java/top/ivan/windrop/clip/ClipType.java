package top.ivan.windrop.clip;

import java.awt.datatransfer.DataFlavor;

/**
 * @author Ivan
 * @since 2023/02/20 13:44
 */
public enum ClipType {
    FILE, STRING, IMAGE, UNKNOWN;
    public static final ClipTypeMatcher U = ts -> UNKNOWN;
    public static final ClipTypeMatcher F = ts -> ts.isDataFlavorSupported(DataFlavor.javaFileListFlavor) ? FILE : null;
    public static final ClipTypeMatcher S = ts -> ts.isDataFlavorSupported(DataFlavor.stringFlavor) ? STRING : null;
    public static final ClipTypeMatcher I = ts -> ts.isDataFlavorSupported(DataFlavor.imageFlavor) ? IMAGE : null;
}
