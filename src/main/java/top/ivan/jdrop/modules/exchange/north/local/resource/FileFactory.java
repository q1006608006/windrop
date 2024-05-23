package top.ivan.jdrop.modules.exchange.north.local.resource;

import org.springframework.util.StringUtils;
import top.ivan.jdrop.framework.util.IDUtils;
import top.ivan.jdrop.framework.util.fs.PathUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public interface FileFactory {
    String MULTI_PATTERN = "( \\(\\d+\\))?";

    FileFactory USER_DOWNLOAD_FACTORY = fromFolder(PathUtils.getDownloadsPath());
    FileFactory USER_DOWNLOAD_RANDOM_NAME_FACTORY = filename -> Paths.get(PathUtils.getDownloadsPath(), IDUtils.get32UUID());

    Path getPath(String filename);

    default File crateFile(String filename) {
        Path path = getPath(filename);
        if (Files.exists(path)) {
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

            path = parent.resolve(repeatFilename(match, ext, offset));
        }
        return path.toFile();
    }

    default String repeatFilename(String name, String ext, long mute) {
        return name + " (" + mute + ")" + (ext == null ? "" : "." + ext);
    }

    static FileFactory fromFolder(String folder) {
        return name -> Paths.get(folder, name);
    }

}
