package top.ivan.jdrop.framework.system.cp2;

import top.ivan.jdrop.framework.util.cache.VolatileHandler;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.util.concurrent.Executors;

public class ClipboardUtils {
    private static final Clipboard CLIPBOARD = Toolkit.getDefaultToolkit().getSystemClipboard();

    private static final VolatileHandler<Transferable> CLIPBOARD_BEAN_HANDLER = new VolatileHandler<>();

    public static ClipType getClipboardBeanType() {
        Transferable ts = getTransfer();
        return ClipTypeSelectorManager.select(ts);
    }


    public static void setClipboardTransfer(Transferable ts) {
//        CLIPBOARD.setContents(ts.getTransferDataFlavors());
        Executors.newWorkStealingPool()
    }


    private synchronized static Transferable getTransfer() {
        Transferable ts = CLIPBOARD.getContents(null);
        CLIPBOARD_BEAN_HANDLER.setValue(ts);
        return ts;
    }
}
