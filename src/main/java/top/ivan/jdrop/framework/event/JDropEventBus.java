package top.ivan.jdrop.framework.event;

import com.google.common.collect.LinkedListMultimap;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import top.ivan.jdrop.framework.event.trigger.TypeTrigger;

@Service
public class JDropEventBus {

    private LinkedListMultimap<JDropEventTrigger<? extends JDropEvent>, JDropEventListener> listenerMap = LinkedListMultimap.create();
    private LinkedListMultimap<TypeTrigger, JDropEventTrigger> typeMap = LinkedListMultimap.create();

    public <T extends JDropEvent> void publish(T event) {

    }

    public <T extends JDropEvent> void registerListener(@NonNull JDropEventListener<T> listener, JDropEventTrigger<T>... triggers) {
        if (null == triggers || triggers.length == 0) {
            triggers = new JDropEventTrigger[]{new TypeTrigger(listener.getListenedType())};
        }

        for (JDropEventTrigger<T> trigger : triggers) {
            listenerMap.put(trigger, listener);
        }
    }

    public Class<?> getTypeClass(JDropEventListener<?> tar) {
        return tar.getListenedType();
    }

}
