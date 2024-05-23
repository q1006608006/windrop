package top.ivan.jdrop.framework.event;

import lombok.Getter;
import top.ivan.jdrop.framework.util.IDUtils;

public abstract class JDropEvent {

    @Getter
    private final String eventId;

    public JDropEvent() {
        this.eventId = IDUtils.get32UUID();
    }

}
