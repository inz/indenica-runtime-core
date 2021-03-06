package eu.indenica.common;

import eu.indenica.events.Event;

public interface PubSub {
    void publish(String sourceComponent, Event event);

    void publishAll(EventEmitter source);

    void registerListener(EventListener listener, String source, Event event);

    void registerListener(EventListener listener, String source,
            String eventType);

    void destroy() throws Exception;
}
