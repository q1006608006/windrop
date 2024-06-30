package top.ivan.jdrop.framework.system.io.clipboard;

import top.ivan.jdrop.framework.exception.UnSupportClipFlavorException;

import java.awt.datatransfer.Transferable;
import java.util.PriorityQueue;

public class ClipFlavorManager {
    private static final PriorityQueue<ClipFlavor> FLAVOR_PRIORITY_QUEUE = new PriorityQueue<>();

    public static void register(ClipFlavor cf) {
        FLAVOR_PRIORITY_QUEUE.add(cf);
    }

    public static ClipFlavor selectFlavor(Transferable ts) throws UnSupportClipFlavorException {
        for (ClipFlavor cf : FLAVOR_PRIORITY_QUEUE) {
            if (cf.isSupported(ts)) {
                return cf;
            }
        }
        throw new UnSupportClipFlavorException("no flavor match");
    }

}
