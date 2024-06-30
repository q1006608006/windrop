package top.ivan.jdrop.framework.system.io.clipboard.transfer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class DataSelection implements Transferable {

    private final Map<DataFlavor, Object> dataMap;
    private final Set<DataFlavor> flavors;

    public DataSelection(Map<DataFlavor, Object> dataMap) {
        this.dataMap = dataMap;
        this.flavors = this.dataMap.keySet();
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return flavors.toArray(new DataFlavor[0]);
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavors.contains(flavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
        if (isDataFlavorSupported(flavor)) {
            Object target = dataMap.get(flavor);
            if (target instanceof Supplier) {
                return ((Supplier<?>) target).get();
            } else {
                return target;
            }
        }
        throw new UnsupportedFlavorException(flavor);
    }

    static Map<DataFlavor, Object> multiSetFlavors(Object data, DataFlavor... flavors) {
        Map<DataFlavor, Object> dataMap = new HashMap<>();
        for (DataFlavor f : flavors) {
            dataMap.put(f, data);
        }
        return dataMap;
    }
}
