package top.ivan.windrop.system.clipboard;

import java.awt.datatransfer.Transferable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Ivan
 * @since 2023/02/20 13:45
 */
public class ClipTypeSelector {
    private final Set<ClipTypeMatcher> matchers;

    /**
     * matchers
     *
     * @param matchers matchers in order
     */
    public ClipTypeSelector(Collection<ClipTypeMatcher> matchers) {
        this.matchers = new LinkedHashSet<>();
        this.matchers.addAll(matchers);
    }

    public ClipType select(Transferable ts) {
        ClipType t;
        for (ClipTypeMatcher m : matchers) {
            if (accessible(t = m.match(ts))) {
                return t;
            }
        }
        return ClipType.UNKNOWN;
    }

    public static boolean accessible(ClipType t) {
        return null != t && ClipType.UNKNOWN != t;
    }
}
