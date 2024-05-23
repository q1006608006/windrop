package top.ivan.jdrop.modules.exchange.north.local.resource;

import java.io.File;

public interface FileResource extends ExchangeResource {
    File getFile();

    String filename();

    String name();
}
