package top.ivan.jdrop.modules.exchange.north.local.resource;

import top.ivan.jdrop.framework.util.Base64Utils;

public class Base64TextResourceWrapper extends TextResource {
    public Base64TextResourceWrapper(TextResource origin) {
        super(origin.name, origin.value, origin.type());
    }

    @Override
    public String getContent() {
        return Base64Utils.decodeBase64Str(super.getContent());
    }
}
