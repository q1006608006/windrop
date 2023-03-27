package top.ivan.windrop.util;

import java.io.File;

/**
 * @author Ivan
 * @since 2023/03/13 16:52
 */
public class FileUtil {

    public static long getFilesLength(File f) {
        return f.isDirectory() ? getDirLength(f) : f.length();
    }

    public static long getDirLength(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            long len = 0;
            for (File file : files) {
                if (file.isDirectory()) {
                    len += getDirLength(file);
                } else {
                    len += file.length();
                }
            }
            return len;
        } else {
            return 0;
        }
    }

}
