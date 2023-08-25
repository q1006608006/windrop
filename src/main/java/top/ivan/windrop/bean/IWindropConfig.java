package top.ivan.windrop.bean;

import java.util.List;

/**
 * @author Ivan
 * @since 2023/07/28 14:14
 */
public interface IWindropConfig {

    int getPort();

    long getMaxFileLength();

    long getTextFileLimit();

    String getEncoding();

    ShortcutApi getShortcutApi();

    List<String> getNetworkInterfaces();

    boolean stepTest();
}
