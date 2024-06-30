package top.ivan.jdrop.framework.system.io.clipboard;

import top.ivan.jdrop.framework.exception.UnSupportClipFlavorException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ClipHolderSpiManager {

    private static final Map<ClipFlavor, ClipHolderSpi<?>> SPI_MAP = new HashMap<>();

    public synchronized static void registerSpi(ClipFlavor flavor, ClipHolderSpi<?> spi) {
        SPI_MAP.put(flavor, new CachedClipHolder<>(spi));
    }

    @SuppressWarnings("unchecked")
    public static <T> ClipHolderSpi<T> findClipHolderSpi(ClipFlavor flavor) throws UnSupportClipFlavorException {
        ClipHolderSpi spi = SPI_MAP.get(flavor);
        if (spi != null && spi.test(flavor)) {
            return spi;
        }
        return (ClipHolderSpi) SPI_MAP.values()
                .stream().filter(s -> !Objects.equals(s, spi))
                .filter(s -> s.test(flavor))
                .findFirst()
                .orElseThrow(() -> new UnSupportClipFlavorException("not suitable spi for: " + flavor));
    }

}
