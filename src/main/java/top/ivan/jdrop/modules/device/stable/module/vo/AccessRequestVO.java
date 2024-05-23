package top.ivan.jdrop.modules.device.stable.module.vo;

import lombok.Data;

@Data
public class AccessRequestVO {
    private final AccessInfoVO info;
    private final String ticket;
    private final String content;

    public AccessRequestVO(AccessInfoVO info, String ticket, String content) {
        this.info = info;
        this.ticket = ticket;
        this.content = content;
    }

    public String getDeviceId() {
        return info.getDeviceId();
    }
}
