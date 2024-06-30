package top.ivan.jdrop.framework.system.io;

import org.springframework.util.StringUtils;
import top.ivan.jdrop.framework.util.IDUtils;
import top.ivan.jdrop.framework.util.fs.PathUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

public class FileFactory {
    String MULTI_PATTERN = "( \\(\\d+\\))?";

    public static FileFactory USER_DOWNLOAD_FACTORY = fromFolder(PathUtils.getDownloadsPath(), false);
    public static FileFactory USER_DOWNLOAD_RANDOM_NAME_FACTORY = new FileFactory(
            filename -> Paths.get(PathUtils.getDownloadsPath(), IDUtils.get32UUID()),
            false
    );
    public static FileFactory TEMP_FACTORY = fromFolder(PathUtils.getTempPath(), true);

    private final Function<String, Path> nameFunction;
    private final boolean repeat;

    public FileFactory(Function<String, Path> func, boolean repeat) {
        this.nameFunction = func;
        this.repeat = repeat;
    }

    public File createFile(String filename) {
        Path path = getPath(filename);
        if (!repeat && Files.exists(path)) {
            path = avoidRepeat(path);
        }
        return path.toFile();
    }

    private Path getPath(String filename) {
        return nameFunction.apply(filename);
    }

    private Path avoidRepeat(Path path) {
        Path parent = path.getParent();
        String originName = path.getFileName().toString();
        String match = StringUtils.stripFilenameExtension(originName);
        String ext = StringUtils.getFilenameExtension(originName);
        String pattern = match + MULTI_PATTERN + (ext == null ? "" : "." + ext);
        long offset;
        try {
            offset = Files.find(parent, 1, (p, a) -> p.getFileName().toString().matches(pattern))
                    .count() + 1;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        path = parent.resolve(parseRepeatFilename(match, ext, offset));
        return path;
    }

    private String parseRepeatFilename(String name, String ext, long mute) {
        return name + " (" + mute + ")" + (ext == null ? "" : "." + ext);
    }

    public static FileFactory fromFolder(String folder, boolean repeat) {
        return new FileFactory(name -> Paths.get(folder, name), repeat);
    }

}
