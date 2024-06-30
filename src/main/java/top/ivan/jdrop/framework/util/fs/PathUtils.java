package top.ivan.jdrop.framework.util.fs;

import javax.swing.*;
import java.nio.file.Paths;

public class PathUtils {

    public static String getDocumentsPath() {
        JFileChooser chooser = new JFileChooser();
        chooser.setVisible(false);
        return chooser.getFileSystemView().getDefaultDirectory().toString();
    }

    public static String getDownloadsPath() {
        return Paths.get(System.getProperties().getProperty("user.home"), "downloads").toString();
    }

    public static String getTempPath() {
        return System.getProperty("java.io.tmpdir");
    }

}
