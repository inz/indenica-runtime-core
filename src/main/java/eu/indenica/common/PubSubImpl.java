package eu.indenica.common;

import java.util.ArrayList;
import java.util.Collection;

import eu.indenica.events.Event;

public class PubSubImpl implements PubSub, EventListener{

	private static PubSubImpl instance;
	private Collection<ListenerSourceEvent>	listeners;
	
	
	private PubSubImpl() {
		listeners = new ArrayList<ListenerSourceEvent>();
	}
	
	public static synchronized PubSub getInstance() {
		if(instance == null) instance = new PubSubImpl();
		return instance;
	}
	
	@Override
	public void publish(RuntimeComponent source, Event event) {
		for(ListenerSourceEvent lse : listeners) {
			if ( (lse.source == null || lse.source.equals(source)) &&
			     (lse.event == null || lse.event.getEventType().equals(event.getEventType())) )
			     	lse.listener.publish(source, event);
		}
	}

	@Override
	public void publishAll(EventEmitter source) {
		source.addEventListener(this);
		
	}

	@Override
	public void registerListener(EventListener listener,
			RuntimeComponent source, Event event) {
		listeners.add(new ListenerSourceEvent(listener, source, event));
		
	}
	

	private class ListenerSourceEvent {
		EventListener listener;
		RuntimeComponent source;
		Event event;
		ListenerSourceEvent(EventListener listener, RuntimeComponent source, Event event) {
			this.listener = listener;
			this.source = source;
			this.event = event;
		}
	}
	
	
}
