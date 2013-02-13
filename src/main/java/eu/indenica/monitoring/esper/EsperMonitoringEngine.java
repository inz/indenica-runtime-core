package eu.indenica.monitoring.esper;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.slf4j.Logger;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.StatementAwareUpdateListener;

import eu.indenica.common.LoggerFactory;
import eu.indenica.common.PubSub;
import eu.indenica.common.PubSubFactory;
import eu.indenica.events.Event;
import eu.indenica.monitoring.MonitoringEngine;
import eu.indenica.monitoring.MonitoringQuery;
import eu.indenica.monitoring.MonitoringQueryImpl;

/**
 * A monitoring engine implementation using the Esper CEP engine.
 * 
 * @author Christian Inzinger
 */
// @EagerInit
// @Scope("COMPOSITE")
// @XmlSeeAlso({ MonitoringQueryImpl.class })
public class EsperMonitoringEngine implements MonitoringEngine,
        StatementAwareUpdateListener {
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
        pubsub = PubSubFactory.getPubSub();
        epService = EPServiceProviderManager.getDefaultProvider();
        if(queries != null)
            for(MonitoringQuery q : queries)
                addQuery(q);
        LOG.info("{} started", getClass().getSimpleName());
    }

    @Destroy
    @Override
    public void destroy() throws Exception {
        LOG.debug("Stopping Monitoring Engine...");
        pubsub.destroy();
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
        addStatement(query);
    }

    /**
     * @see eu.indenica.monitoring.MonitoringEngine#startQuery(java.lang.String)
     */
    @Override
    public void startQuery(String queryName) {
        epService.getEPAdministrator().getStatement(queryName).stop();
    }

    /**
     * @see eu.indenica.monitoring.MonitoringEngine#stopQuery(java.lang.String)
     */
    @Override
    public void stopQuery(String queryName) {
        epService.getEPAdministrator().getStatement(queryName).start();
    }

    /**
     * Add the given {@link MonitoringQuery} to the monitoring engine
     * 
     * @param query
     *            the query to be added
     */
    private void addStatement(MonitoringQuery query) {
        epService.getEPAdministrator()
                .createEPL(query.getStatement(), query.getName())
                .addListener(this);
    }

    /**
     * Register all input event types of the given {@link MonitoringQuery}.
     * 
     * @param query
     *            the query to register input event types for
     */
    private void registerInputEventTypes(MonitoringQuery query) {
        for(String eventType : query.getInputEventTypes()) {
            String source = null;
            if(eventType.contains(",")) {
                String[] split = eventType.split(",", 2);
                eventType = split[1].trim();
                source = split[0].trim();
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
            pubsub.registerListener(this, source, eventType);
        }
    }

    private Event previousEvent = null;

    @Override
    public void update(EventBean[] newEvents, EventBean[] oldEvents,
            EPStatement statement, EPServiceProvider epServiceProvider) {
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
        pubsub.publish(statement.getName(), (Event) event.getUnderlying());
    }

}
