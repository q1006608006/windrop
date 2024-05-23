package top.ivan.jdrop.framework.event;

public interface JDropEventTrigger<T extends JDropEvent> {
    boolean test(T t);
}
