package top.ivan.windrop.system.io;

import top.ivan.windrop.system.io.FilesWatcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

            try {
                if (!dir.exists()) {
                    Files.createDirectory(Paths.get(dir.toURI()));
                }
                Files.createFile(Paths.get(file.toURI()));
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

    public boolean isExist() {
        return this.watchFile.exists();
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
