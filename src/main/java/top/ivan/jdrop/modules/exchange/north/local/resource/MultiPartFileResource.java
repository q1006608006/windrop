package top.ivan.jdrop.modules.exchange.north.local.resource;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.codec.multipart.FilePart;
import top.ivan.jdrop.framework.constant.MediaType;
import top.ivan.jdrop.framework.util.cache.InitObject;

import java.io.*;

public class MultiPartFileResource implements FileResource {

    private final FilePart part;
    private final MediaType type;
    private final FileFactory factory;
    private final InitObject<File> fileHandler;
    private final InitObject<String> sha256Handler;

    public MultiPartFileResource(FilePart part, MediaType type, FileFactory factory) {
        this.part = part;
        this.type = type;
        this.factory = factory;
        this.fileHandler = new InitObject<>(() -> crateFile());
        this.sha256Handler = new InitObject<>(() -> calculateSha256());
    }

    @Override
    public String sha256() {
        return sha256Handler.get();
    }

    @Override
    public String filename() {
        return part.filename();
    }

    @Override
    public String name() {
        return part.name();
    }

    @Override
    public File getFile() {
        return fileHandler.get();
    }

    private File crateFile() {
        File file = factory.crateFile(part.filename());
        part.transferTo(file).subscribe();
        return file;
    }

    private String calculateSha256() {
        try (InputStream in = new BufferedInputStream(new FileInputStream(getFile()))) {
            return DigestUtils.sha256Hex(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MediaType type() {
        return type;
    }

}
