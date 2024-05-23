package top.ivan.jdrop.modules.exchange.north.local.resource;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.codec.multipart.FormFieldPart;
import top.ivan.jdrop.framework.constant.MediaType;

public class TextResource implements ExchangeResource {

    final String name;
    final String value;
    private final MediaType type;

    public TextResource(String name, String value, MediaType type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    public TextResource(FormFieldPart part, MediaType type) {
        this(part.name(), part.value(), type);
    }

    @Override
    public String sha256() {
        return DigestUtils.sha256Hex(getContent());
    }

    @Override
    public MediaType type() {
        return type;
    }

    public String getContent() {
        return value;
    }
}
