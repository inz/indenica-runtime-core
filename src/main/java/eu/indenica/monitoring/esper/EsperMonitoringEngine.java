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
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

import eu.indenica.common.LoggerFactory;
import eu.indenica.common.PubSub;
import eu.indenica.common.PubSubFactory;
import eu.indenica.common.RuntimeComponent;
import eu.indenica.events.Event;
import eu.indenica.monitoring.MonitoringEngine;
import eu.indenica.monitoring.MonitoringQuery;
import eu.indenica.monitoring.MonitoringQueryImpl;

@Scope("COMPOSITE")
@EagerInit
@XmlSeeAlso({MonitoringQueryImpl.class})
public class EsperMonitoringEngine implements MonitoringEngine, UpdateListener {
	private final static Logger LOG = LoggerFactory.getLogger();
	private PubSub pubsub;
	private EPServiceProvider epService;

	private MonitoringQueryImpl[] queries;
	
	/**
	 * @param queries the queries to set
	 */
	@Property
	public void setQueries(MonitoringQueryImpl[] queries) {
		this.queries = queries;
	}

	@Init
	@Override
	public void init() throws Exception {
		LOG.info("Starting {}", this.getClass().getSimpleName());
		this.pubsub = PubSubFactory.getPubSub();
		epService = EPServiceProviderManager.getDefaultProvider();
		for(MonitoringQuery q : queries)
			addQuery(q);
	}

	@Destroy
	@Override
	public void destroy() throws Exception {
		epService.removeAllServiceStateListeners();
		epService.removeAllStatementStateListeners();
		epService.destroy();
	}

	@Override
	public void eventReceived(RuntimeComponent source, Event event) {
		epService.getEPRuntime().sendEvent(event);
	}

	@Override
	public void addQuery(MonitoringQuery query) {
		LOG.info("Adding query {}", query);
		for(String eventType : query.getInputEventTypes()) {
			String source = null;
			if(eventType.contains(",")) {
				String[] split = eventType.split(",", 2);
				eventType = split[1].trim();
				source = split[0].trim();
				// FIXME: Correctly get RuntimeComponent reference to register.
			}

			pubsub.registerListener(this, null, eventType);
		}
		EPStatement statement =
				epService.getEPAdministrator().createEPL(query.getStatement());
		statement.addListener(this);
	}

	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		// TODO: Create proper event type from rule, result.
		/**
		 * Rule: + list of attributes/how to re-create event transform rule!.
		 * 
		 * http://esper.codehaus.org/esper-4.6.0/doc/reference/en-US/html_single
		 * /index.html#functionreference-transpose
		 */
		EventBean event = newEvents[0];
		pubsub.publish(this, (Event) event.getUnderlying());
	}

}
