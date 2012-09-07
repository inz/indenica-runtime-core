package eu.indenica.common;

import eu.indenica.monitoring.Event;

public interface PubSub {
	void publish(RuntimeComponent source, Event event);
	void publishAll(EventEmitter source);
	void registerListener(EventListener listener, RuntimeComponent source, Event event);
}
