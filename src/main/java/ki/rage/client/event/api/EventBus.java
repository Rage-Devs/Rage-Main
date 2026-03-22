package ki.rage.client.event.api;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

// крашил иногда т.к не было так называемой потокобезопасности

public class EventBus {
    private final Map<Class<?>, List<EventSubscriber>> subscribers = new ConcurrentHashMap<>();

    public void register(Object listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(EventTarget.class) && method.getParameterCount() == 1) {
                method.setAccessible(true);
                Class<?> eventType = method.getParameterTypes()[0];
                subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                        .add(new EventSubscriber(listener, method));
            }
        }
    }

    public void unregister(Object listener) {
        for (List<EventSubscriber> list : subscribers.values()) {
            list.removeIf(sub -> sub.getListener() == listener);
        }
    }

    public void post(Object event) {
        List<EventSubscriber> list = subscribers.get(event.getClass());
        if (list == null) return;
        for (EventSubscriber sub : list) {
            try {
                sub.getMethod().invoke(sub.getListener(), event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class EventSubscriber {
        private final Object listener;
        private final Method method;

        public EventSubscriber(Object listener, Method method) {
            this.listener = listener;
            this.method = method;
        }

        public Object getListener() {
            return listener;
        }

        public Method getMethod() {
            return method;
        }
    }
}
