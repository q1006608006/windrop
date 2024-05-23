package top.ivan.jdrop.framework.event.trigger;

import lombok.EqualsAndHashCode;
import top.ivan.jdrop.framework.event.JDropEvent;
import top.ivan.jdrop.framework.event.JDropEventTrigger;

@EqualsAndHashCode
public final class TypeTrigger implements JDropEventTrigger<JDropEvent> {
    private final Class<?> type;

    public TypeTrigger(Class<? extends JDropEvent> type) {
        this.type = type;
    }

    @Override
    public boolean test(JDropEvent jDropEvent) {
        return type.isInstance(jDropEvent);
    }
}
