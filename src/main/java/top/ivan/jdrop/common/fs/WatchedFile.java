package top.ivan.jdrop.common.fs;

import lombok.Getter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WatchedFile {
    @Getter
    private final File file;

    private final FilesWatcher watcher;

    public WatchedFile(String dataFile) {
        this(dataFile, new FilesWatcher(), false);
    }

    public WatchedFile(String dataFile, boolean create) {
        this(dataFile, new FilesWatcher(), create);
    }

    public WatchedFile(String dataFile, FilesWatcher watcher, boolean create) {
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
        this.file = file;
        this.watcher = watcher;
        this.watcher.register(this.file);
        if (create) {
            try {
                this.create();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean isUpdate() {
        return this.watcher.isUpdated(this.file);
    }

    public boolean isExist() {
        return this.file.exists();
    }

    public boolean create() throws IOException {
        if (isExist()) {
            return false;
        }
        return this.file.createNewFile();
    }

    public byte[] load() throws IOException {
        if (this.file.exists()) {
            return Files.readAllBytes(this.file.toPath());
        } else {
            throw new IOException("un exists file");
        }
    }

    public Path getPath() {
        return file.toPath();
    }

    public void syncWatcher() {
        watcher.syncFile(file);
    }

    public void write(byte[] data) throws IOException {
        try (FileOutputStream output = new FileOutputStream(file, false)) {
            output.write(data);
        }
    }

}
