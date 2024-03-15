package top.ivan.windrop.system.io;

import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Ivan
 * @since 2021/11/08 15:44
 */
public abstract class InitialResource extends AbstractResource {

    @Override
    public String getDescription() {
        throw new UnsupportedOperationException("unsupported in lazy-resource");
    }

    @Override
    public InputStream getInputStream() throws IOException {
        throw new UnsupportedOperationException("unsupported in lazy-resource");
    }

    public abstract boolean isReady();

    public abstract Resource takeResource();

    public Resource getResource() {
        while (!isReady()) {
        }
        return takeResource();
    }
}
