package org.kookies.mirai.plugin.event;

import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.Listener;
import org.kookies.mirai.core.event.EventBus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class MiraiEventBus implements EventBus {
    private final JavaPlugin kookie;
    private final Map<Class<?>, Listener<?>> subscriptions = new ConcurrentHashMap<>();

    public MiraiEventBus(JavaPlugin kookie) {
        this.kookie = kookie;
    }

    @Override
    public <E extends Event> void subscribe(Class<E> eventType, Consumer<E> handler) {
        Listener<E> listener = GlobalEventChannel.INSTANCE.parentScope(kookie).subscribeAlways(eventType, handler);
        subscriptions.put(eventType, listener);
    }

    @Override
    public <E extends Event> void unsubscribe(Class<E> eventType) {
        Listener<?> listener = subscriptions.remove(eventType);
        if (listener != null) {
            listener.complete();
        }
    }

    @Override
    public <E extends Event> void publish(E event) {
        throw new UnsupportedOperationException("Mirai does not support manual event publishing");
    }
}
