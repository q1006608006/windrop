package top.ivan.jdrop.framework.event.trigger;

import lombok.EqualsAndHashCode;
import top.ivan.jdrop.framework.event.JDropEvent;
import top.ivan.jdrop.framework.event.JDropEventTrigger;

@EqualsAndHashCode
public class AcceptAllTrigger implements JDropEventTrigger<JDropEvent> {

    public static AcceptAllTrigger INSTANCE = new AcceptAllTrigger();

    private AcceptAllTrigger() {
    }

    @Override
    public boolean test(JDropEvent jDropEvent) {
        return true;
    }
}
