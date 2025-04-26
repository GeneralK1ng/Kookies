package org.kookies.mirai.core.event;

import net.mamoe.mirai.event.Event;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

/**
 * The core event dispatcher responsible for coordinating event distribution between an {@link EventBus}
 * and registered event handlers. This class provides thread-safe registration and dispatch mechanisms,
 * ensuring events are routed to appropriate handlers based on their type. Handlers are invoked in the
 * thread context of the event bus's dispatch cycle.
 * <p>
 * Maintains a concurrent registry of event handlers using {@link ConcurrentHashMap} and
 * {@link CopyOnWriteArraySet} to guarantee thread safety during handler registration and event processing.
 * @author General_K1ng
 */
public class CoreEventDispatcher {
    private final EventBus eventBus;
    /**
     * A thread-safe registry mapping event types to their associated handlers.
     * Uses {@link CopyOnWriteArraySet} to ensure safe iteration during concurrent modifications.
     */
    private final Map<Class<?>, Set<Consumer<?>>> handlers = new ConcurrentHashMap<>();

    /**
     * Constructs a dispatcher instance bound to the specified event bus.
     *
     * @param eventBus The event bus for event subscription and reception. Must not be {@code null}.
     * @throws NullPointerException if {@code eventBus} is {@code null}.
     */
    public CoreEventDispatcher(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * Registers a handler for events of the specified type. Upon the first registration for an event type,
     * automatically subscribes to the event bus for that type. Subsequent registrations add handlers without
     * affecting existing subscriptions.
     * <p>
     * This method is thread-safe and idempotent for the same {@code eventType} and {@code handler} pair.
     *
     * @param <E>        The event type, extending {@link Event}.
     * @param eventType  The {@link Class} object representing the event type. Must not be {@code null}.
     * @param handler    The {@link Consumer} to handle events of type {@code E}. Must not be {@code null}.
     * @throws NullPointerException if {@code eventType} or {@code handler} is {@code null}.
     */
    public <E extends Event> void registerHandler(Class<E> eventType, Consumer<E> handler) {
        handlers.computeIfAbsent(eventType, k -> new CopyOnWriteArraySet<>()).add(handler);
        eventBus.subscribe(eventType, this::dispatchEvent);
    }

    /**
     * Dispatches an event to all registered handlers of its specific type. This method is invoked
     * by the event bus when a subscribed event is published. Handlers are executed synchronously
     * in the event bus's dispatch thread.
     * <p>
     * Type safety is enforced by the handler registration process, ensuring runtime compatibility.
     *
     * @param <E>   The concrete event type.
     * @param event The event instance to dispatch. Must not be {@code null}.
     */
    private <E extends Event> void dispatchEvent(E event) {
        Set<Consumer<?>> consumers = handlers.get(event.getClass());
        if (consumers != null) {
            consumers.forEach(consumer -> ((Consumer<E>) consumer).accept(event));
        }
    }
}
