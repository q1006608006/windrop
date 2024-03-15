package top.ivan.windrop.system.tray;

import top.ivan.windrop.bean.FileInfo;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Ivan
 * @description
 * @date 2021/3/15
 */
public class TrayFileUploader {

    private static final int MAX_SIZE = 128;

    private static final Map<String, FileInfo> fileMap = new LinkedHashMap<String, FileInfo>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > MAX_SIZE;
        }
    };

    private static File HOME_FILE = FileSystemView.getFileSystemView().getHomeDirectory();

    public static File showFileFrame(Component parent) {
        JFileChooser fileChooser = new JFileChooser();

        fileChooser.setCurrentDirectory(HOME_FILE);
        fileChooser.setDialogTitle("请选择要上传的文件...");
        fileChooser.setApproveButtonText("确定");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        if (fileChooser.showOpenDialog(parent) == 0) {
            File selectorFile = fileChooser.getSelectedFile();
            HOME_FILE = selectorFile.getParentFile();
            return selectorFile;
        }

        return null;
    }

    public static FileInfo takeByKey(String key) {
        return fileMap.get(key);
    }

    public static void remove(String key) {
        fileMap.remove(key);
    }

}
