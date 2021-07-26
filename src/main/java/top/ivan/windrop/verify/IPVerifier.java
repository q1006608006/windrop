package top.ivan.windrop.verify;

import lombok.extern.slf4j.Slf4j;
import top.ivan.windrop.util.WatchedFile;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ivan
 * @description
 * @date 2021/2/23
 */
@Slf4j
public class IPVerifier {

    private volatile List<String> accessList;
    private final WatchedFile watchedFile;

    public IPVerifier(String ipListPath) {
        watchedFile = new WatchedFile(ipListPath);
        log.debug("加载白名单文件: '{}'", watchedFile.getFile().getAbsolutePath());
        if (!init()) {
            log.warn("白名单初始化失败");
        }
    }

    private boolean init() {
        if (!watchedFile.isExist()) {
            accessList = null;
            log.warn("{} not exist", watchedFile.getFile().getName());
            return false;
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(watchedFile.getPath());
        } catch (IOException e) {
            log.error("can not init access list", e);
            return false;
        } finally {
            watchedFile.sync();
        }

        List<String> newAccessList = new ArrayList<>();
        for (String line : lines) {
            if (line.trim().startsWith("#")) {
                continue;
            }
            if (!line.contains(">")) {
                newAccessList.add(line);
            } else {
                try {
                    String start = line.substring(0, line.indexOf('>'));
                    int length = Integer.parseInt(line.substring(line.indexOf('>') + 1));
                    String zone = start.replaceAll("(\\d+\\.\\d+\\.\\d+)\\.(\\d+)", "$1");
                    int pos = Integer.parseInt(start.replaceAll("(\\d+\\.\\d+\\.\\d+)\\.(\\d+)", "$2"));
                    for (int i = 0; i < length; i++) {
                        newAccessList.add(zone + "." + (pos + i));
                    }
                } catch (Exception e) {
                    log.warn("can not parse text '{}'", line);
                }
            }
        }
        accessList = newAccessList;
        return true;
    }

    public boolean accessible(String ip) {
        if (watchedFile.isUpdate()) {
            synchronized (this) {
                if (watchedFile.isUpdate()) {
                    init();
                }
            }
            return accessible(ip);
        }
        if (accessList == null || accessList.isEmpty()) {
            return true;
        } else {
            return accessList.contains(ip);
        }
    }

}
