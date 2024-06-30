package top.ivan.jdrop.framework.system.io.clipboard;

import top.ivan.jdrop.framework.util.cache.LambdaHandler;
import top.ivan.windrop.util.Pair;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Objects;

public class CachedClipHolder<T> implements ClipHolderSpi<T> {

    private final ClipHolderSpi<T> core;
    private LambdaHandler<Pair<Transferable, T>> cacheHandler;

    public CachedClipHolder(ClipHolderSpi<T> spi) {
        this.core = spi;
        this.cacheHandler = new LambdaHandler<>();
    }

    @Override
    public T parse(Transferable ts) throws IOException, UnsupportedFlavorException {
        Pair<Transferable, T> cache = cacheHandler.getValue();
        if (!Objects.equals(cache.getKey(), ts)) {
            cacheHandler.setValue(new Pair<>(ts, core.parse(ts)));
        }
        return cacheHandler.getValue().getValue();
    }


}
