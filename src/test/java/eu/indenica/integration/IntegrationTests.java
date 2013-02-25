/**
 * 
 */
package eu.indenica.integration;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

import eu.indenica.adaptation.AdaptationEngine;
import eu.indenica.adaptation.AdaptationRule;
import eu.indenica.adaptation.EsperFactTransformer;
import eu.indenica.adaptation.Fact;
import eu.indenica.adaptation.FactTransformer;
import eu.indenica.adaptation.drools.DroolsAdaptationEngine;
import eu.indenica.common.LoggerFactory;
import eu.indenica.common.TestUtils;
import eu.indenica.events.Event;
import eu.indenica.events.EventOne;
import eu.indenica.messaging.DiscoveryNameProvider;
import eu.indenica.messaging.MessageBroker;
import eu.indenica.monitoring.MonitoringEngine;
import eu.indenica.monitoring.MonitoringQuery;
import eu.indenica.monitoring.esper.EsperMonitoringEngine;

/**
 * Test complete scenarios w/ stubbed external components.
 * 
 * @author Christian Inzinger
 */
public class IntegrationTests {
    private static final Logger LOG = LoggerFactory.getLogger();
    private Semaphore msgWaitLock;
    private String hostName;
    private DiscoveryNameProvider nameProvider;
    private MessageBroker broker;
    private PlatformAdapter platformAdapter;
    private MonitoringEngine monitoringEngine;
    private FactTransformer factTransformer;
    private AdaptationEngine adaptationEngine;

    /**
     */
    @BeforeClass
    public static void setUpBeforeClass() {
        TestUtils.setLogLevels();
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        msgWaitLock = new Semaphore(0);
        hostName = "test-node-" + System.currentTimeMillis();
        setupBroker();
        setupPlatformAdapter();
        setupMonitoringEngine();
        setupFactTransformer();
        setupAdaptationEngine();
    }

    /**
     * @throws Exception
     */
    private void setupBroker() throws Exception {
        nameProvider =
                new DiscoveryNameProvider(getClass().getSimpleName() + "-"
                        + System.currentTimeMillis());
        broker = new MessageBroker(nameProvider);
    }

    /**
     * @throws Exception
     */
    private void setupPlatformAdapter() throws Exception {
        platformAdapter = new PlatformAdapter();
        platformAdapter.setHostName(hostName);
        platformAdapter.setId("platform-" + System.currentTimeMillis());
        platformAdapter.setName("test-platform");
        Collection<Class<? extends Event>> emittedEventTypes =
                Lists.newArrayList();
        emittedEventTypes.add(EventOne.class);
        platformAdapter.setEmittedEventTypes(emittedEventTypes);
        platformAdapter.init();
    }
    
    /**
     * @throws Exception
     */
    private void setupMonitoringEngine() throws Exception {
        monitoringEngine = new EsperMonitoringEngine();
        monitoringEngine.setHostName(hostName);
        monitoringEngine.init();
        
        // TODO: Add monitoring query!
    }

    /**
     * @throws Exception
     */
    private void setupFactTransformer() throws Exception {
        factTransformer = new EsperFactTransformer();
        factTransformer.setHostName(hostName);
        factTransformer.init();
        
        // TODO: Add fact transformation query!
    }
    
    /**
     * @throws Exception
     */
    private void setupAdaptationEngine() throws Exception {
        adaptationEngine = new DroolsAdaptationEngine();
        adaptationEngine.setHostName(hostName);
        ((DroolsAdaptationEngine) adaptationEngine).setGlobal("lock",
                msgWaitLock);
        adaptationEngine.init();
        
        // TODO: Add adaptation rule!
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        adaptationEngine.destroy();
        factTransformer.destroy();
        monitoringEngine.destroy();
        platformAdapter.destroy();
        broker.destroy();
        assertThat(msgWaitLock.drainPermits(), is(0));
    }

    /**
     * A simple control chain should work.
     * 
     * <p>
     * A {@link PlatformAdapter} emits a monitoring event that is received by a
     * {@link MonitoringQuery} in the {@link MonitoringEngine}. The query emits
     * an event to be transformed into a {@link Fact} by a
     * {@link FactTransformer}. The fact is then consumed by an
     * {@link AdaptationRule} in the {@link AdaptationEngine} which performs an
     * action on the {@link PlatformAdapter}.
     */
    @Test
    public void testSimpleRoundTrip() throws Exception {
        LOG.info("Platform emits event...");
        EventOne event = new EventOne();
        event.setAttr1("message-" + System.currentTimeMillis());
        platformAdapter.emitEvent(event);
        
        LOG.info("Wait for monitoring query to receive event...");
        assertThat(msgWaitLock.tryAcquire(2, TimeUnit.SECONDS), is(true));

        LOG.info("Fact transformer receives event from query...");
        assertThat("nothing", is("implemented"));

        LOG.info("Fact is provided to adaptation rule...");
        assertThat("nothing", is("implemented"));

        LOG.info("Adaptation rule performs action...");
        assertThat("nothing", is("implemented"));

        LOG.info("Platform receives adaptation action event.");
        assertThat("nothing", is("implemented"));
    }

}
