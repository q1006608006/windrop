package top.ivan.windrop.util;

import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Ivan
 * @since 2021/11/08 15:44
 */
public class LazyResource extends AbstractResource {

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return null;
    }
}
