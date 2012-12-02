/**
 * 
 */
package eu.indenica.adaptation;

import javax.xml.bind.annotation.XmlSeeAlso;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Scope;
import org.slf4j.Logger;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

import eu.indenica.common.LoggerFactory;
import eu.indenica.common.PubSub;
import eu.indenica.common.PubSubFactory;
import eu.indenica.common.RuntimeComponent;
import eu.indenica.events.Event;

/**
 * @author Christian Inzinger
 * 
 */
@EagerInit
@Scope("COMPOSITE")
@XmlSeeAlso({ FactRuleImpl.class })
public class EsperFactTransformer implements FactTransformer, UpdateListener {
	private final static Logger LOG = LoggerFactory.getLogger();
	
	private PubSub pubsub;
	private EPServiceProvider epService;
	private FactRule[] rules;

	@Init
	@Override
	public void init() throws Exception {
		LOG.debug("Starting {}...", getClass().getSimpleName());
		this.pubsub = PubSubFactory.getPubSub();
		epService = EPServiceProviderManager.getDefaultProvider();
		for(FactRule rule : rules)
			addRule(rule);
		LOG.info("{} started", getClass().getSimpleName());
	}

	/* (non-Javadoc)
	 * @see eu.indenica.common.RuntimeComponent#destroy()
	 */
	@Destroy
	@Override
	public void destroy() throws Exception {
		LOG.debug("Stopping Fact Transformer...");
		// TODO Auto-generated method stub
		LOG.info("Fact Transformer stopped.");
	}

	@Property
	public void setFactRules(final FactRuleImpl[] rules) {
		LOG.debug("Setting fact rules: {}", rules);
		this.rules = rules;
	}

	/**
	 * @param rule
	 */
	private void addRule(final FactRule rule) {
		LOG.info("Adding rule {}", rule);
		registerInputEventTypes(rule);
		addStatement(rule);
	}

	/**
	 * @param rule
	 */
	private void addStatement(final FactRule rule) {
		for(String stmt : rule.getStatement().trim().split(";")) {
			epService.getEPAdministrator().createEPL(stmt).addListener(this);
		}
	}

	/**
	 * @param rule
	 */
	private void registerInputEventTypes(final FactRule rule) {
		for(String eventType : rule.getInputEventTypes()) {
			String source = null;
			if(eventType.contains(",")) {
				String[] split = eventType.split(",", 2);
				eventType = split[1].trim();
				source = split[0].trim();
				// FIXME: Correctly get RuntimeComponent reference to register.
				LOG.trace("Found source: {}", source);
			}

			try {
				Class<?> eventTypeClass = Class.forName(eventType);
				LOG.info("Loaded class {}", eventTypeClass);
				epService.getEPAdministrator().getConfiguration()
						.addEventType(eventTypeClass);
				epService.getEPAdministrator().getConfiguration()
						.addImport(eventTypeClass);
			} catch(ClassNotFoundException e) {
				LOG.warn("Could not find class {}!", eventType);
				e.printStackTrace();
			}
			pubsub.registerListener(this, null, eventType);
		}
	}

	
	private Event previousEvent = null;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.espertech.esper.client.UpdateListener#update(com.espertech.esper.
	 * client.EventBean[], com.espertech.esper.client.EventBean[])
	 */
	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		EventBean event = newEvents[0];
//		if(event.getUnderlying().equals(previousEvent))
//			return;
//		previousEvent = (Event) event.getUnderlying();
		LOG.trace("Event object: {}", event);
		LOG.info("Publishing fact event {}", event.getUnderlying());
		pubsub.publish(this, (Event) event.getUnderlying());
	}

	/* (non-Javadoc)
	 * @see eu.indenica.common.EventListener#eventReceived(eu.indenica.common.RuntimeComponent, eu.indenica.events.Event)
	 */
	@Override
	public void eventReceived(RuntimeComponent source, Event event) {
		LOG.debug("Event {} received from {}", event, source);
		epService.getEPRuntime().sendEvent(event);
	}
}
