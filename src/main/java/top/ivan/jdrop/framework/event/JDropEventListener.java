package top.ivan.jdrop.framework.event;

import top.ivan.jdrop.framework.util.cache.InitObject;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class JDropEventListener<T extends JDropEvent> {

    protected InitObject<Class<? extends JDropEvent>> listenTypeHandler;

    public JDropEventListener() {
        this.listenTypeHandler = new InitObject<>(this::initListenType);
    }

    protected Class<? extends JDropEvent> getListenedType() {
        return this.listenTypeHandler.get();
    }

    private Class<? extends JDropEvent> initListenType() {
        Type superClass = getClass().getGenericSuperclass();

        if (superClass instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) superClass;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (actualTypeArguments.length > 0) {
                return (Class<? extends JDropEvent>) actualTypeArguments[0];
            }
        }
        return JDropEvent.class;
    }

    public abstract void subscribe(T event);


}
