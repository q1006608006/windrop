package top.ivan.windrop.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class WatchedFile {
    private final File watchFile;

    private final FilesWatcher watcher;

    public WatchedFile(String dataFile) {
        this(dataFile, new FilesWatcher());
    }

    public WatchedFile(String dataFile, FilesWatcher watcher) {
        File file = new File(dataFile);
        if (!file.exists()) {
            File dir = file.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        this.watchFile = file;
        this.watcher = watcher;
        this.watcher.register(this.watchFile);
    }

    public boolean isUpdate() {
        return this.watcher.isUpdated(this.watchFile);
    }

    public byte[] load() throws IOException {
        if (this.watchFile.exists()) {
            return Files.readAllBytes(this.watchFile.toPath());
        } else {
            return null;
        }
    }

    public File getFile() {
        return watchFile;
    }

    public Path getPath() {
        return watchFile.toPath();
    }

    public void sync() {
        watcher.syncFile(watchFile);
    }

}
