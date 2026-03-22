package ki.rage.client.event.api;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventBus {
    private final Map<Class<? extends Event>, List<MethodData>> listeners = new HashMap<>();

    public void register(Object obj) {
        for (Method method : obj.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(EventTarget.class)) {
                continue;
            }
            if (method.getParameterCount() != 1) {
                continue;
            }
            Class<?> eventClass = method.getParameterTypes()[0];
            if (!Event.class.isAssignableFrom(eventClass)) {
                continue;
            }
            method.setAccessible(true);
            listeners.computeIfAbsent((Class<? extends Event>) eventClass, k -> new ArrayList<>())
                    .add(new MethodData(obj, method));
        }
    }

    public void unregister(Object obj) {
        listeners.values().forEach(list -> list.removeIf(data -> data.instance == obj));
    }

    public void post(Event event) {
        List<MethodData> list = listeners.get(event.getClass());
        if (list == null) {
            return;
        }
        for (MethodData data : list) {
            try {
                data.method.invoke(data.instance, event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class MethodData {
        final Object instance;
        final Method method;

        MethodData(Object instance, Method method) {
            this.instance = instance;
            this.method = method;
        }
    }
}
