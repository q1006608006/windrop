package top.ivan.windrop.clip;

import java.awt.datatransfer.Transferable;

/**
 * @author Ivan
 * @since 2023/02/20 14:02
 */
public interface ClipTypeMatcher {
    ClipType match(Transferable ts);
}
