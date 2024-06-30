package top.ivan.jdrop.framework.system.io.clipboard;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public interface ClipHolderSpi<T> {
    T parse(Transferable ts) throws IOException, UnsupportedFlavorException;

    default boolean test(ClipFlavor ts) {
        return true;
    }
}
