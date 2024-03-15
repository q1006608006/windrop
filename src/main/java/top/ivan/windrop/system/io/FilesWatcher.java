package top.ivan.windrop.system.io;

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

    public void thenIfUpdated(File file, Consumer<File> accept) {
        if (isUpdated(file)) {
            synchronized (fileMap) {
                if (fileMap.get(file) != file.lastModified()) {
                    fileMap.put(file, file.lastModified());
                    if (null != accept) {
                        accept.accept(file);
                    }
                }
            }
        }
    }

    public void syncFile(File file) {
        fileMap.computeIfPresent(file, (k, v) -> file.lastModified());
    }

}