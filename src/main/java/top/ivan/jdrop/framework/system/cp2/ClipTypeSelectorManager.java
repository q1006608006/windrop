package top.ivan.jdrop.framework.system.cp2;

import io.vavr.collection.PriorityQueue;
import lombok.Data;

import java.awt.datatransfer.Transferable;

public class ClipTypeSelectorManager {
    @Data
    private static class OrderSelector implements Comparable<OrderSelector> {
        private final ClipTypeSelector selector;
        private final int order;

        @Override
        public int compareTo(OrderSelector o) {
            return order - o.order;
        }
    }

    private static final int DEFAULT_ORDER = Integer.MAX_VALUE;

    private static final PriorityQueue<OrderSelector> SELECTOR_QUEUE = PriorityQueue.empty();

    public static void register(ClipTypeSelector selector, int order) {
        SELECTOR_QUEUE.enqueue(new OrderSelector(selector, order));
    }

    public static void register(ClipTypeSelector selector) {
        SELECTOR_QUEUE.enqueue(new OrderSelector(selector, DEFAULT_ORDER));
    }

    public static ClipType select(Transferable ts) {
        for (OrderSelector os : SELECTOR_QUEUE) {
            ClipType type = os.selector.select(ts);
            if (type != null) {
                return type;
            }
        }
        return ClipType.UNKNOWN;
    }

}
