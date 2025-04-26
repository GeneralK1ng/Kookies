package org.kookies.mirai.core.event;

import net.mamoe.mirai.event.Event;

import java.util.function.Consumer;

public interface EventBus {
    <E extends Event> void subscribe(Class<E> eventType, Consumer<E> handler);
    <E extends Event> void unsubscribe(Class<E> eventType);
    <E extends Event> void publish(E event);
}
