/**
 * 
 */
package eu.indenica.monitoring.esper;

import static eu.indenica.common.TestUtils.createEventListener;
import static eu.indenica.common.TestUtils.setLogLevels;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.jms.Message;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

import eu.indenica.common.ActivemqPubSub;
import eu.indenica.common.LoggerFactory;
import eu.indenica.common.PubSub;
import eu.indenica.events.Event;
import eu.indenica.events.EventTwo;
import eu.indenica.messaging.DiscoveryNameProvider;
import eu.indenica.messaging.ManagementClient;
import eu.indenica.messaging.MessageBroker;
import eu.indenica.monitoring.MonitoringEngine;
import eu.indenica.monitoring.MonitoringQuery;
import eu.indenica.monitoring.MonitoringQueryImpl;

/**
 * Test the various bits and pieces of the {@link EsperMonitoringEngine}.
 * 
 * @author Christian Inzinger
 */
public class EsperMonitoringEngineTest {
    private static Logger LOG = LoggerFactory.getLogger();
    private Semaphore msgWaitLock;
    private DiscoveryNameProvider nameProvider;
    private PubSub pubSub;
    private ManagementClient mgmtClient;
    private MessageBroker broker;
    private MonitoringEngine monitoringEngine;
    private String nodeName;
    private Collection<Event> observedEvents;

    @BeforeClass
    public static void setUpBeforeClass() {
        setLogLevels();
    }

    @Before
    public void setUp() throws Exception {
        msgWaitLock = new Semaphore(0);
        nameProvider =
                new DiscoveryNameProvider(getClass()
                        .getSimpleName() + "-" + System.currentTimeMillis());
        broker = new MessageBroker(nameProvider);
        pubSub = new ActivemqPubSub();
        nodeName = "test-node-" + System.currentTimeMillis();
        mgmtClient = new ManagementClient(nodeName, "test-mgmt");
        monitoringEngine = new EsperMonitoringEngine();
        ((EsperMonitoringEngine) monitoringEngine).setHostName(nodeName);
        monitoringEngine.init();
        observedEvents = Lists.newArrayList();
    }

    @After
    public void tearDown() throws Exception {
        assertThat(msgWaitLock.drainPermits(), is(0));
        monitoringEngine.destroy();
        pubSub.destroy();
        broker.destroy();
    }

    /**
     * The monitoring engine should allow to add a {@link MonitoringQuery} and
     * run it. The query should receive events and emit them as appropriate
     * 
     * @throws Exception
     *             if something goes wrong
     */
    @Test
    public void testAddSimpleQuery() throws Exception {
        String queryName = addSimpleQueryForEventOne();
        Event event = null;

        createEventListener(pubSub, queryName, new EventTwo().getEventType(),
                observedEvents, msgWaitLock);

        LOG.info("Sending event...");
        event = sendEventTwoToSimpleQuery("message");
        verifyEventObserved(observedEvents, event);
    }

    /**
     * A client should be able to activate and deactivate a query.
     * 
     * @throws Exception
     *             if something goes wrong
     */
    @Test
    public void testClientManageSimpleQuery() throws Exception {
        String queryName = addSimpleQueryForEventOne();
        Event event = null;

        createEventListener(pubSub, queryName, new EventTwo().getEventType(),
                observedEvents, msgWaitLock);

        LOG.info("Deactivating query...");
        monitoringEngine.stopQuery(queryName);

        LOG.info("Sending event...");
        event = sendEventTwoToSimpleQuery("should not arrive");
        verifyEventNotObserved(observedEvents, event);

        LOG.info("Activating query...");
        monitoringEngine.startQuery(queryName);

        LOG.info("Sending event...");
        event = sendEventTwoToSimpleQuery("should arrive");
        verifyEventObserved(observedEvents, event);
    }

    /**
     * A management client should be able to activate and deactivate a query.
     * 
     * @throws Exception
     *             if something goes wrong
     */
    @Test
    public void testMgmtClientManageSimpleQuery() throws Exception {
        String queryName = addSimpleQueryForEventOne();
        Event event = null;

        createEventListener(pubSub, queryName, new EventTwo().getEventType(),
                observedEvents, msgWaitLock);

        {
            LOG.info("Deactivating query...");
            Message stopQuery = mgmtClient.createMapMessage();
            stopQuery.setStringProperty("command", "stopQuery");
            stopQuery.setStringProperty("queryName", queryName);
            mgmtClient.sendCommand(stopQuery, nodeName,
                    MonitoringEngine.SERVICE_NAME);
        }

        LOG.info("Sending event...");
        event = sendEventTwoToSimpleQuery("should not arrive");
        verifyEventNotObserved(observedEvents, event);

        {
            LOG.info("Activating query...");
            Message startQuery = mgmtClient.createMapMessage();
            startQuery.setStringProperty("command", "startQuery");
            startQuery.setStringProperty("queryName", queryName);
            mgmtClient.sendCommand(startQuery, nodeName,
                    MonitoringEngine.SERVICE_NAME);
        }

        LOG.info("Sending event...");
        event = sendEventTwoToSimpleQuery("should arrive");
        verifyEventObserved(observedEvents, event);
    }

    /**
     * @param observedEvents
     * @param event
     * @throws InterruptedException
     */
    private void verifyEventNotObserved(Collection<Event> observedEvents,
            Event event) throws InterruptedException {
        LOG.trace("Verifying that event did not arrive...");
        assertThat(msgWaitLock.tryAcquire(2, TimeUnit.SECONDS), is(false));
        assertThat(observedEvents, not(hasItem((Event) event)));
        assertThat(observedEvents.size(), is(0));
    }

    /**
     * @param observedEvents
     * @param event
     * @throws InterruptedException
     */
    private void verifyEventObserved(Collection<Event> observedEvents,
            Event event) throws InterruptedException {
        LOG.trace("Verifying that event arrived...");
        assertThat(msgWaitLock.tryAcquire(2, TimeUnit.SECONDS), is(true));
        assertThat(observedEvents, hasItem((Event) event));
    }

    /**
     * @return
     */
    private Event sendEventTwoToSimpleQuery(String message) {
        EventTwo event = new EventTwo();
        event.setMessage(message + " " + System.currentTimeMillis());
        pubSub.publish("input", event);
        return event;
    }

    /**
     * @return the name of the created query
     */
    private String addSimpleQueryForEventOne() {
        LOG.debug("Adding simple query...");
        String queryName = "query-" + System.currentTimeMillis();
        MonitoringQueryImpl query = new MonitoringQueryImpl();
        query.setName(queryName);
        query.setInputEventTypes(new String[] { "input,"
                + EventTwo.class.getCanonicalName() });
        query.setOutputEventTypes(new String[] { EventTwo.class
                .getCanonicalName() });
        query.setStatement("select * from EventTwo");
        monitoringEngine.addQuery(query);
        return queryName;
    }

}
