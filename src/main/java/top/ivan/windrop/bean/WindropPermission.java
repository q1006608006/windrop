package top.ivan.windrop.bean;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Ivan
 * @since 2023/06/25 10:51
 */
@Data
public class WindropPermission {
    public static final String PUSH_CONFIRM_TEXT = "PUSH_CONFIRM_TEXT";
    public static final String PUSH_CONFIRM_IMAGE = "PUSH_CONFIRM_IMAGE";
    public static final String PUSH_CONFIRM_FILE = "PUSH_CONFIRM_FILE";

    public static final String PULL_CONFIRM_TEXT = "PULL_CONFIRM_TEXT";
    public static final String PULL_CONFIRM_IMAGE = "PULL_CONFIRM_IMAGE";
    public static final String PULL_CONFIRM_FILE = "PULL_CONFIRM_FILE";

    public static final String PUSH_NOTIFY_TEXT = "PUSH_NOTIFY_TEXT";
    public static final String PUSH_NOTIFY_IMAGE = "PUSH_NOTIFY_IMAGE";
    public static final String PUSH_NOTIFY_FILE = "PUSH_NOTIFY_FILE";
    public static final String PULL_NOTIFY_TEXT = "PULL_NOTIFY_TEXT";
    public static final String PULL_NOTIFY_IMAGE = "PULL_NOTIFY_IMAGE";
    public static final String PULL_NOTIFY_FILE = "PULL_NOTIFY_FILE";

    public static final String AUTO_OPEN_URL = "AUTO_OPEN_URL";
    public static final String AUTO_OPEN_IMAGE = "AUTO_OPEN_IMAGE";
    public static final String AUTO_OPEN_FILE = "AUTO_OPEN_FILE";

    public static final Map<String, Integer> PERMISSIONS = new LinkedHashMap<>();

    static {
        PERMISSIONS.put(PUSH_CONFIRM_TEXT, 0);
        PERMISSIONS.put(PUSH_CONFIRM_IMAGE, 1);
        PERMISSIONS.put(PUSH_CONFIRM_FILE, 2);
        PERMISSIONS.put(PULL_CONFIRM_TEXT, 3);
        PERMISSIONS.put(PULL_CONFIRM_IMAGE, 4);
        PERMISSIONS.put(PULL_CONFIRM_FILE, 5);
        PERMISSIONS.put(PUSH_NOTIFY_TEXT, 6);
        PERMISSIONS.put(PUSH_NOTIFY_IMAGE, 7);
        PERMISSIONS.put(PUSH_NOTIFY_FILE, 8);
        PERMISSIONS.put(PULL_NOTIFY_TEXT, 9);
        PERMISSIONS.put(PULL_NOTIFY_IMAGE, 10);
        PERMISSIONS.put(PULL_NOTIFY_FILE, 11);
        PERMISSIONS.put(AUTO_OPEN_URL, 12);
        PERMISSIONS.put(AUTO_OPEN_IMAGE, 13);
        PERMISSIONS.put(AUTO_OPEN_FILE, 14);
    }

    private long permission = 0;

    public boolean testAccess(String req) {
        return (getLong(req) & permission) > 0;
    }

    public void access(String req) {
        permission |= getLong(req);
    }

    public void prevent(String req) {
        permission &= ~getLong(req);
    }

    private long getLong(String req) {
        return PERMISSIONS.containsKey(req) ? 1L << PERMISSIONS.get(req) : 0;
    }

}
