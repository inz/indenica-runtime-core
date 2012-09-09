package eu.indenica.monitoring.esper;

import com.espertech.esper.client.*;
import eu.indenica.common.*;
import eu.indenica.events.Event;
import eu.indenica.monitoring.*;

public class EsperMonitoringEngine implements MonitoringEngine, UpdateListener {

	private PubSub pubsub;
	private EPServiceProvider epService;
	
	@Override
	public void init() throws Exception {
		this.pubsub = PubSubFactory.getPubSub();
		epService = EPServiceProviderManager.getDefaultProvider();
	}

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
	public void addRule(MonitoringQuery rule) {
		EPStatement statement = epService.getEPAdministrator().createEPL(rule.getStatement());
		statement.addListener(this);
		//TODO: register for all event types.
	}

	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		//TODO: Create proper event type from rule, result.
		/**
		 * Rule: + list of attributes/how to re-create event
		 * transform rule!.
		 * 
		 * http://esper.codehaus.org/esper-4.6.0/doc/reference/en-US/html_single/index.html#functionreference-transpose
		 */
		EventBean event = newEvents[0];
		pubsub.publish(this, (Event)event.getUnderlying());
	}


}
