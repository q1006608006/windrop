package top.ivan.windrop.svc;

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
        init();
    }

    private void init() {
        if (!watchedFile.isExist()) {
            accessList = null;
            return;
        }
        try {
            List<String> lines = Files.readAllLines(watchedFile.getPath());
            List<String> newAccessList = new ArrayList<>();
            for (String line : lines) {
                if (!line.contains(">")) {
                    newAccessList.add(line);
                } else {
                    String start = line.substring(0, line.indexOf('>'));
                    int length = Integer.parseInt(line.substring(line.indexOf('>') + 1));
                    String zone = start.replaceAll("(\\d+\\.\\d+\\.\\d+)\\.(\\d+)", "$1");
                    int pos = Integer.parseInt(start.replaceAll("(\\d+\\.\\d+\\.\\d+)\\.(\\d+)", "$2"));
                    for (int i = 0; i < length; i++) {
                        newAccessList.add(zone + "." + (pos + i));
                    }
                }
            }
            accessList = newAccessList;
        } catch (IOException e) {
            log.error("读取白名单文件失败", e);
        } finally {
            watchedFile.sync();
        }
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
