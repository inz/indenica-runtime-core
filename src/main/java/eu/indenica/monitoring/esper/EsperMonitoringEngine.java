package eu.indenica.monitoring.esper;

import com.espertech.esper.client.*;
import eu.indenica.common.*;
import eu.indenica.monitoring.*;

public class EsperMonitoringEngine implements MonitoringEngine, UpdateListener {

	private PubSub pubsub;
	private EPServiceProvider epService;
	
	@Override
	public void init(PubSub pubsub) throws Exception {
		this.pubsub = pubsub;
		epService = EPServiceProviderManager.getDefaultProvider();
	}

	@Override
	public void destroy() throws Exception {
		epService.removeAllServiceStateListeners();
        epService.removeAllStatementStateListeners(); 
		epService.destroy();
	}

	@Override
	public void publish(RuntimeComponent source, Event event) {
		epService.getEPRuntime().sendEvent(event);
	}

	@Override
	public void addRule(MonitoringRule rule) {
		EPStatement statement = epService.getEPAdministrator().createEPL(rule.getStatement());
		statement.addListener(this);
	}

	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		EventBean event = newEvents[0];		
		pubsub.publish(this, (Event)event.getUnderlying());
	}


}
