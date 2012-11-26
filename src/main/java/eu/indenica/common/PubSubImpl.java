package eu.indenica.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import eu.indenica.events.Event;

public class PubSubImpl implements PubSub, EventListener {
	private static PubSubImpl instance;
	private Collection<ListenerSourceEvent>	listeners;
	private ExecutorService notifierPool = Executors.newCachedThreadPool();
	
	private PubSubImpl() {
		listeners = new ArrayList<ListenerSourceEvent>();
	}
	
	public static synchronized PubSub getInstance() {
		if(instance == null) instance = new PubSubImpl();
		return instance;
	}
	
	@Override
	public void publish(final RuntimeComponent source, final Event event) {
		for(final ListenerSourceEvent lse : listeners) {
			if ( (lse.source == null || lse.source.equals(source)) &&
			     (lse.eventType == null || lse.eventType.equals(event.getEventType())) )
			     	notifierPool.submit(new Callable<Void>() {
						public Void call() throws Exception {
							lse.listener.eventReceived(source, event);
							return null;
						}});
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

	/* (non-Javadoc)
	 * @see eu.indenica.common.PubSub#destroy()
	 */
	@Override
	public void destroy() throws InterruptedException {
		notifierPool.shutdown();
		notifierPool.awaitTermination(2, TimeUnit.SECONDS);
//		notifierPool.shutdownNow();
	}
}
