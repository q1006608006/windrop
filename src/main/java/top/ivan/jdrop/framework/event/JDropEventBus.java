package top.ivan.jdrop.framework.event;

import com.google.common.collect.LinkedListMultimap;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import top.ivan.jdrop.framework.event.trigger.AcceptAllTrigger;
import top.ivan.jdrop.framework.event.trigger.TypeTrigger;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JDropEventBus {

    private LinkedListMultimap<JDropEventTrigger, JDropEventListener<?>> listenerMap = LinkedListMultimap.create();
    private LinkedListMultimap<TypeTrigger, JDropEventTrigger> typeMap = LinkedListMultimap.create();

    public <T extends JDropEvent> void publish(T event) {
        Set<JDropEventListener> set = typeMap.keys().stream()
                .filter(t -> t.test(event))
                .map(typeMap::get)
                .flatMap(List::stream)
                .filter(t -> t.test(event))
                .map(listenerMap::get)
                .flatMap(List::stream)
                .collect(Collectors.toSet());

        set.forEach(l -> l.subscribe(event));
    }

    public <T extends JDropEvent> void registerListener(@NonNull JDropEventListener<T> listener, JDropEventTrigger<T>... triggers) {
        if (null == triggers || triggers.length == 0) {
            triggers = new JDropEventTrigger[]{AcceptAllTrigger.INSTANCE};
        }

        TypeTrigger typeTrigger = new TypeTrigger(listener.getListenedType());
        for (JDropEventTrigger<T> trigger : triggers) {
            typeMap.put(typeTrigger, trigger);
            listenerMap.put(trigger, listener);
        }
    }

    public Class<?> getTypeClass(JDropEventListener<?> tar) {
        return tar.getListenedType();
    }


    public static void main(String[] args) {

    }
}
