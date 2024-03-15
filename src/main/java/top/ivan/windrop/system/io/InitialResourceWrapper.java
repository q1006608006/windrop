package top.ivan.windrop.system.io;

import org.springframework.core.io.FileSystemResource;

import java.io.File;

public class InitialResourceWrapper extends InitialResource {

    private File file;

    public InitialResourceWrapper() {
    }

    @Override
    public boolean isReady() {
        return file != null;
    }

    public void complete(File file) {
        this.file = file;
    }

    @Override
    public org.springframework.core.io.Resource takeResource() {
        return new FileSystemResource(file);
    }
}