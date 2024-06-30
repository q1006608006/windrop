package top.ivan.jdrop.framework.system.io.clipboard;

import top.ivan.jdrop.framework.exception.UnSupportClipFlavorException;
import top.ivan.jdrop.framework.system.io.FileFactory;
import top.ivan.jdrop.framework.system.io.clipboard.transfer.FileImageSelection;
import top.ivan.jdrop.framework.system.io.clipboard.transfer.FileSelection;
import top.ivan.jdrop.framework.system.io.clipboard.transfer.TextSelection;
import top.ivan.jdrop.framework.system.io.clipboard.transfer.UrlSelection;
import top.ivan.jdrop.framework.util.cache.VolatileHandler;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ClipboardUtils {
    private static final Clipboard CLIPBOARD = Toolkit.getDefaultToolkit().getSystemClipboard();
    private static final VolatileHandler<Transferable> DATA_CACHE = new VolatileHandler<>();

    static {
        ClipboardUtils.registerClipboardSpi(ClipFlavor.FILE_IMAGE, ts -> (List<File>) ts.getTransferData(DataFlavor.javaFileListFlavor));
        ClipboardUtils.registerClipboardSpi(ClipFlavor.WEB_IMAGE, ts -> (List<File>) Collections.singletonList(
                TransferImageUtils.webImageToFile(ts, FileFactory.TEMP_FACTORY))
        );
        ClipboardUtils.registerClipboardSpi(ClipFlavor.IMAGE, ts -> (List<File>) Collections.singletonList(
                TransferImageUtils.imageToFile(ts, FileFactory.TEMP_FACTORY))
        );
        ClipboardUtils.registerClipboardSpi(ClipFlavor.FILES, ts -> (List<File>) ts.getTransferData(DataFlavor.javaFileListFlavor));
        ClipboardUtils.registerClipboardSpi(ClipFlavor.STRING, ts -> (String) ts.getTransferData(DataFlavor.stringFlavor));
    }

    public static <T> void registerClipboardSpi(ClipFlavor flavor, ClipHolderSpi<T> spi) {
        ClipFlavorManager.register(flavor);
        ClipHolderSpiManager.registerSpi(flavor, spi);
    }

    public synchronized static void setClipboard(Transferable ts) {
        CLIPBOARD.setContents(ts, null);
        DATA_CACHE.setValue(ts);
    }

    public static void setText(String text) {
        setClipboard(new TextSelection(text));
    }

    public static void setImageFiles(List<File> files) {
        setClipboard(new FileImageSelection(files));
    }

    public static void setUrl(String url) {
        setClipboard(new UrlSelection(url));
    }

    public static void setFiles(List<File> files) {
        setClipboard(new FileSelection(files));
    }

    public synchronized static ClipFlavor getClipboardFlavor() throws UnSupportClipFlavorException {
        Transferable ts = CLIPBOARD.getContents(null);
        DATA_CACHE.setValue(ts);
        return ClipFlavorManager.selectFlavor(ts);
    }

    public static <T> T getClipboardObject(ClipFlavor flavor) throws UnSupportClipFlavorException {
        Transferable ts = DATA_CACHE.getValue();
        ClipHolderSpi<T> spi = ClipHolderSpiManager.findClipHolderSpi(flavor);
        try {
            return spi.parse(ts);
        } catch (IOException | UnsupportedFlavorException e) {
            throw new UnSupportClipFlavorException("parse clip data error", e);
        }
    }

}
