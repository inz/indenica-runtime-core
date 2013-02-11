package eu.indenica.monitoring.esper;

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
import eu.indenica.events.Event;
import eu.indenica.monitoring.MonitoringEngine;
import eu.indenica.monitoring.MonitoringQuery;
import eu.indenica.monitoring.MonitoringQueryImpl;

@Scope("COMPOSITE")
@EagerInit
@XmlSeeAlso({ MonitoringQueryImpl.class })
public class EsperMonitoringEngine implements MonitoringEngine, UpdateListener {
	private final static Logger LOG = LoggerFactory.getLogger();
	private PubSub pubsub;
	private EPServiceProvider epService;

	private MonitoringQuery[] queries;

	/**
	 * @param queries
	 *            the queries to set
	 */
	@Property
	public void setQueries(MonitoringQueryImpl[] queries) {
		this.queries = queries;
	}

	@Init
	@Override
	public void init() throws Exception {
		LOG.debug("Starting {}", this.getClass().getSimpleName());
		this.pubsub = PubSubFactory.getPubSub();
		epService = EPServiceProviderManager.getDefaultProvider();
		for(MonitoringQuery q : queries)
			addQuery(q);
		LOG.info("{} started", getClass().getSimpleName());
	}

	@Destroy
	@Override
	public void destroy() throws Exception {
		LOG.debug("Stopping Monitoring Engine...");
		epService.removeAllServiceStateListeners();
		epService.removeAllStatementStateListeners();
		epService.destroy();
		LOG.info("Monitoring Engine stopped.");
	}

	@Override
	public void eventReceived(String source, Event event) {
		LOG.debug("Event {} received from {}", event, source);
		epService.getEPRuntime().sendEvent(event);
	}

	@Override
	public void addQuery(MonitoringQuery query) {
		LOG.info("Adding query {}", query);
		registerInputEventTypes(query);
		addStatements(query);
	}

	/**
	 * @param query
	 */
	private void addStatements(MonitoringQuery query) {
		for(String stmt : query.getStatement().trim().split(";")) {
			epService.getEPAdministrator().createEPL(stmt).addListener(this);
		}
	}

	/**
	 * @param query
	 */
	private void registerInputEventTypes(MonitoringQuery query) {
		for(String eventType : query.getInputEventTypes()) {
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

	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		// TODO: Create proper event type from rule, result.
		/**
		 * Rule: + list of attributes/how to re-create event transform rule!.
		 * 
		 * http://esper.codehaus.org/esper-4.6.0/doc/reference/en-US/html_single
		 * /index.html#functionreference-transpose
		 */
		LOG.trace("{} new events, {} old events",
				newEvents != null ? newEvents.length : 0,
				oldEvents != null ? oldEvents.length : 0);
		LOG.trace("newEvents: {}, oldEvents: {}", newEvents, oldEvents);
		if(newEvents == null) {
			LOG.info("No new events received.");
			return;
		}

		EventBean event = newEvents[0];
		// LOG.debug("event: {}", event.getUnderlying());
		// if(event.getUnderlying().equals(previousEvent))
		// return;
		previousEvent = (Event) event.getUnderlying();
		LOG.info("Publishing event {} (previous event: {})",
				event.getUnderlying(), previousEvent);
		pubsub.publish(this.getClass().getName(), (Event) event.getUnderlying());
	}

}
