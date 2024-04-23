package top.ivan.jdrop.common.fs;

import javax.swing.*;

public class PathUtils {

    public static String getDocumentsPath() {
        JFileChooser chooser = new JFileChooser();
        chooser.setVisible(false);
        return chooser.getFileSystemView().getDefaultDirectory().toString();
    }


}
