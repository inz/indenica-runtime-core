package eu.indenica.common;

import java.util.ArrayList;
import java.util.Collection;

import eu.indenica.events.Event;

public class PubSubImpl implements PubSub, EventListener {

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
			     (lse.eventType == null || lse.eventType.equals(event.getEventType())) )
			     	lse.listener.eventReceived(source, event);
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
	
	@Override
	public void registerListener(EventListener listener,
			RuntimeComponent source, String eventType) {
		listeners.add(new ListenerSourceEvent(listener, source, eventType));
		
	}
	

	private class ListenerSourceEvent {
		EventListener listener;
		RuntimeComponent source;
		String eventType;
		ListenerSourceEvent(EventListener listener, RuntimeComponent source, Event event) {
			this(listener, source, event.getEventType());
		}

		ListenerSourceEvent(EventListener listener, RuntimeComponent source, String eventType) {
			this.listener = listener;
			this.source = source;
			this.eventType = eventType;
		}
	}


	/* (non-Javadoc)
	 * @see eu.indenica.common.EventListener#eventReceived(eu.indenica.common.RuntimeComponent, eu.indenica.events.Event)
	 */
	@Override
	public void eventReceived(RuntimeComponent source, Event event) {
		publish(source, event);
	}
}
