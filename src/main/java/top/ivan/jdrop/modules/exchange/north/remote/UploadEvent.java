package top.ivan.jdrop.modules.exchange.north.remote;

import lombok.Data;
import top.ivan.jdrop.framework.event.JDropEvent;
import top.ivan.jdrop.modules.device.stable.module.vo.AccessInfoVO;
import top.ivan.jdrop.modules.exchange.north.local.resource.ExchangeResource;

import java.util.List;

@Data
public class UploadEvent extends JDropEvent {

    private final AccessInfoVO info;
    private final List<? extends ExchangeResource> resourceList;

    public UploadEvent(AccessInfoVO info, List<? extends ExchangeResource> resources) {
        this.info = info;
        this.resourceList = resources;
    }
}
