package top.ivan.windrop.util;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * @author Ivan
 * @description
 * @date 2021/3/10
 */
public class FilesWatcher {

    private final Map<File, Long> fileMap;

    public FilesWatcher() {
        fileMap = new ConcurrentHashMap<>();
    }

    public void register(File file) {
        fileMap.putIfAbsent(file, file.lastModified());
    }

    public boolean isUpdated(File file) {
        if (fileMap.containsKey(file)) {
            return fileMap.get(file) != file.lastModified();
        }
        return false;
    }

    public void ifUpdatedThen(File file, Consumer<File> accept) {
        if (fileMap.containsKey(file)) {
            if (fileMap.get(file) != file.lastModified()) {
                synchronized (file) {
                    if (fileMap.get(file) != file.lastModified()) {
                        fileMap.put(file, file.lastModified());
                        if (null != accept) {
                            accept.accept(file);
                        }
                    }
                }
            }
        }
    }

    public void syncFile(File file) {
        if (fileMap.containsKey(file)) {
            fileMap.put(file, file.lastModified());
        }
    }

}