/**
 * 
 */
package eu.indenica.integration;

import java.util.Collection;

import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;
import org.slf4j.Logger;

import com.google.common.collect.Sets;

import eu.indenica.adaptation.Action;
import eu.indenica.common.ActionListener;
import eu.indenica.common.EventEmitter;
import eu.indenica.common.EventListener;
import eu.indenica.common.LoggerFactory;
import eu.indenica.common.PubSub;
import eu.indenica.common.PubSubFactory;
import eu.indenica.common.RuntimeComponent;
import eu.indenica.events.ActionEvent;
import eu.indenica.events.Event;

/**
 * The integration interface base class provides for common logic to communicate
 * with the VSP runtime infrastructure. It holds an identifier for the
 * integrated platform, and can implement custom code for communication.
 * Standard adapters for different technologies are provides, such as
 * {@link WebServiceAdapter}, {@link JmsAdapter}, and {@link AmqpAdapter}.
 * 
 * <p>
 * An adapter specifies the capabilities of the underlying service platforms by
 * enumerating the monitoring {@link Event}s it emits, as well as the adaptation
 * {@link Action}s it supports.
 * 
 * 
 * @author Christian Inzinger
 * 
 */
public class PlatformAdapter implements RuntimeComponent, EventEmitter,
		ActionListener, EventListener {
	private final static Logger LOG = LoggerFactory.getLogger();
	protected Collection<Class<? extends Event>> emittedEventTypes = Sets
			.newHashSet();
	protected Collection<Class<? extends Action>> adaptationActions = Sets
			.newHashSet();
	protected Collection<EventListener> eventListeners = Sets.newHashSet();

	protected String id;
	protected String name;

	@Property
	protected String endpointAddress;

	@Reference
	protected WireFormatAdapter wireStrategy;
	private PubSub pubSub = PubSubFactory.getPubSub();

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.indenica.common.RuntimeComponent#init()
	 */
	@Override
	public void init() throws Exception {
		LOG.debug("Starting component {}", this);
		pubSub.registerListener(this, null, new ActionEvent(null));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.indenica.common.RuntimeComponent#destroy()
	 */
	@Override
	public void destroy() throws Exception {
		LOG.debug("Stopping component {}", this);
	}

	/**
	 * @return the emittedEventTypes
	 */
	public Collection<Class<? extends Event>> getEmittedEventTypes() {
		return emittedEventTypes;
	}

	/**
	 * @return the adaptationActions
	 */
	public Collection<Class<? extends Action>> getAdaptationActions() {
		return adaptationActions;
	}

	public void addEventListener(EventListener listener) {
		eventListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.indenica.common.EventEmitter#emitEvent(eu.indenica.monitoring.Event)
	 */
	public void emitEvent(Event event) {
		pubSub.publish(this.getName(), event);
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	protected void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	protected void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the endpointAddress
	 */
	public String getEndpointAddress() {
		return endpointAddress;
	}

	/**
	 * @param endpointAddress
	 *            the endpointAddress to set
	 */
	public void setEndpointAddress(String endpointAddress) {
		this.endpointAddress = endpointAddress;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.indenica.common.ActionListener#performAction(eu.indenica.adaptation
	 * .Action)
	 */
	@Override
	public void performAction(Action action) {
		wireStrategy.performAction(endpointAddress, action);
	}

	/* (non-Javadoc)
	 * @see eu.indenica.common.EventListener#publish(eu.indenica.common.RuntimeComponent, eu.indenica.events.Event)
	 */
	@Override
	public void eventReceived(String source, Event event) {
		// TODO Auto-generated method stub
		
	}

}
