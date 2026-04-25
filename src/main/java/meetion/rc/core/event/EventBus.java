package meetion.rc.core.event;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public final class EventBus {

    private static final Map<Class<? extends Event>, List<Listener>> listeners = new HashMap<>();

    private EventBus() {}

    public static void register(Object subscriber) {
        for (Method method : subscriber.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(EventHandler.class)) continue;
            if (method.getParameterCount() != 1) continue;
            Class<?> param = method.getParameterTypes()[0];
            if (!Event.class.isAssignableFrom(param)) continue;

            @SuppressWarnings("unchecked")
            Class<? extends Event> eventClass = (Class<? extends Event>) param;
            method.setAccessible(true);

            byte priority = method.getAnnotation(EventHandler.class).priority();
            Listener listener = new Listener(subscriber, method, priority);

            listeners.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>()).add(listener);
            listeners.get(eventClass).sort(Comparator.comparingInt(l -> l.priority));
        }
    }

    public static void unregister(Object subscriber) {
        for (List<Listener> list : listeners.values()) {
            list.removeIf(l -> l.source == subscriber);
        }
    }

    public static <T extends Event> T post(T event) {
        List<Listener> list = listeners.get(event.getClass());
        if (list == null || list.isEmpty()) return event;

        for (Listener listener : list) {
            try {
                listener.method.invoke(listener.source, event);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (event.isCancelled()) break;
        }
        return event;
    }

    private record Listener(Object source, Method method, byte priority) {}
}
