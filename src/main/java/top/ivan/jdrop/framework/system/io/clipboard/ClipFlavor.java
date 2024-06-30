package top.ivan.jdrop.framework.system.io.clipboard;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@EqualsAndHashCode
@ToString
public class ClipFlavor implements Comparable<ClipFlavor> {

    private static final int BASE_ORDER = Integer.MAX_VALUE;

    //----------------------BASE TYPE------------------------
    public static final ClipFlavor FILES = new ClipFlavor(BASE_ORDER, DataFlavor.javaFileListFlavor);
    public static final ClipFlavor STRING = new ClipFlavor(BASE_ORDER, DataFlavor.stringFlavor);
    public static final ClipFlavor IMAGE = new ClipFlavor(BASE_ORDER - 1, DataFlavor.imageFlavor);
    public static final ClipFlavor HTML = new ClipFlavor(STRING.priorityOrder - 1, DataFlavor.allHtmlFlavor);

    //----------------------MULTI TYPE------------------------
    public static final ClipFlavor FILE_IMAGE = new ClipFlavor(IMAGE.priorityOrder - 1, FILES, IMAGE);
    public static final ClipFlavor WEB_IMAGE = new ClipFlavor(FILE_IMAGE.priorityOrder, HTML, IMAGE);



    final List<List<DataFlavor>> flavorConditions;

    final int priorityOrder;

    public ClipFlavor(int priorityOrder, List<List<DataFlavor>> flavorConditions) {
        this.flavorConditions = flavorConditions;
        this.priorityOrder = priorityOrder;
    }

    public ClipFlavor(int priorityOrder, DataFlavor... flavors) {
        this(priorityOrder, Collections.singletonList(Arrays.asList(flavors)));
    }

    public ClipFlavor(int priorityOrder, ClipFlavor... flavors) {
        this(priorityOrder, Stream.of(flavors).map(f -> f.flavorConditions)
                .reduce((a, b) -> mergeList(a, b)).get()
        );
    }


    /**
     * for extend
     */
    ClipFlavor(int priorityOrder) {
        this(priorityOrder, Collections.emptyList());
    }


    public boolean isSupported(Transferable ts) {
        boolean match = false;
        for (List<DataFlavor> fs : flavorConditions) {
            for (DataFlavor f : fs) {
                if (!(match = ts.isDataFlavorSupported(f))) {
                    break;
                }
            }
            if (match) {
                break;
            }
        }
        return match;
    }

    public boolean isBasedOn(ClipFlavor flavor) {

        for (List<DataFlavor> inner : this.flavorConditions) {
            Set<DataFlavor> innerSet = new HashSet<>(inner);
            for (List<DataFlavor> outer : flavor.flavorConditions) {
                if (innerSet.containsAll(outer)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int compareTo(ClipFlavor o) {
        return this.priorityOrder - o.priorityOrder;
    }

    private static <T> List<List<T>> mergeList(List<List<T>> left, List<List<T>> right) {
        return left.stream().flatMap(a -> right.stream()
                .map(b -> io.vavr.collection.List.ofAll(a)
                        .appendAll(b)
                        .toJavaList()
                )).collect(Collectors.toList());
    }
}
