/**
 * 
 */
package eu.indenica.messaging;

import static eu.indenica.common.TestUtils.createEventListener;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.util.Collection;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

import eu.indenica.common.ActivemqPubSub;
import eu.indenica.common.LoggerFactory;
import eu.indenica.common.PubSub;
import eu.indenica.common.TestUtils;
import eu.indenica.events.Event;
import eu.indenica.events.EventOne;

/**
 * Messaging Fabric test suite.
 * 
 * Tests ActiveMQ broker/client connectivity, broker interconnect.
 * 
 * @author Christian Inzinger
 */
public class ConnectivityTest {
    private final static Logger LOG = LoggerFactory.getLogger();

    public static class CustomBrokerUriPubSub extends ActivemqPubSub {
        public CustomBrokerUriPubSub(URI brokerUri) throws Exception {
            super(brokerUri);
        }
    }


    private String applicationName;
    private DiscoveryNameProvider nameProvider;
    private MessageBroker defaultBroker;
    private PubSub defaultPubSub;
    private Semaphore msgWaitLock;
    private Collection<Event> observedEvents;

    @BeforeClass
    public static void setUpBeforeClass() {
        TestUtils.setLogLevels();
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        applicationName =
                getClass().getSimpleName() + "-" + System.currentTimeMillis();
        nameProvider = new DiscoveryNameProvider(applicationName);
        defaultBroker = new MessageBroker(nameProvider);
        defaultPubSub = new ActivemqPubSub();
        msgWaitLock = new Semaphore(0);
        observedEvents = Lists.newArrayList();

        assertThat(defaultBroker, is(notNullValue()));
        assertThat(defaultPubSub, is(notNullValue()));
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        observedEvents.clear();
        msgWaitLock.drainPermits();
        defaultPubSub.destroy();
        defaultBroker.destroy();
    }

    /**
     * Test if events can be sent and received at all.
     * 
     * One broker, default messaging behavior. Send and receive events using
     * same pubsub instance.
     * 
     * @throws Exception
     */
    @Test
    public void testSimpleConnection() throws Exception {
        createEventListener(defaultPubSub, observedEvents, msgWaitLock);

        LOG.debug("Sending empty message...");
        defaultPubSub.publish(null, new EventOne());
        assertThat(msgWaitLock.tryAcquire(2, TimeUnit.SECONDS), is(true));
        assertThat(observedEvents.size(), is(1));

        LOG.debug("Sending message w/ content");
        Event e = new EventOne();
        ((EventOne) e).setAttr1("a value" + System.currentTimeMillis());
        defaultPubSub.publish(null, e);
        assertThat(msgWaitLock.tryAcquire(2, TimeUnit.SECONDS), is(true));
        assertThat(observedEvents, hasItem(e));

        observedEvents.clear();
        int nEvents = 13;
        for(int i = 0; i < nEvents; i++) {
            Event event = new EventOne();
            ((EventOne) event).setAnAttribute(i);
            ((EventOne) event)
                    .setAttr1("message " + System.currentTimeMillis());
            LOG.debug("Sending message with event: {}...", e);
            defaultPubSub.publish(null, event);
        }
        msgWaitLock.tryAcquire(nEvents, 2, TimeUnit.SECONDS);
        assertThat(observedEvents.size(), is(nEvents));
    }

    /**
     * Client should be able to establish connection to broker using multicast
     * discovery.
     * 
     * Messages should be delivered between pubsub instances.
     * 
     * @throws Exception
     */
    @Test
    public void testMcastDiscovery() throws Exception {
        PubSub mcastPubSub =
                new CustomBrokerUriPubSub(
                        nameProvider.getMulticastDiscoveryUri());
        assertThat(mcastPubSub, is(notNullValue()));
        createEventListener(mcastPubSub, observedEvents, msgWaitLock);

        LOG.debug("Send event in same pubsub instance...");
        Event e = new EventOne();
        mcastPubSub.publish(null, e);
        assertThat(msgWaitLock.tryAcquire(2, TimeUnit.SECONDS), is(true));
        assertThat(observedEvents, hasItem(e));

        LOG.debug("Send event from defaultPubSub to mcastPubSub...");
        e = new EventOne();
        defaultPubSub.publish(null, e);
        assertThat(msgWaitLock.tryAcquire(2, TimeUnit.SECONDS), is(true));
        assertThat(observedEvents, hasItem(e));

        observedEvents.clear();

        createEventListener(defaultPubSub, observedEvents, msgWaitLock);

        LOG.debug("Send event from defaultPubSub to be received by both...");
        e = new EventOne();
        defaultPubSub.publish(null, e);
        msgWaitLock.tryAcquire(2, 2, TimeUnit.SECONDS);
        assertThat(observedEvents.size(), is(2));
    }

    /**
     * Multiple brokers should be able to discover each other and pass messages
     * for consumers.
     * 
     * @throws Exception
     */
    @Test
    public void testBrokerDiscovery() throws Exception {
        MessageBroker secondBroker = new MessageBroker(nameProvider);
        PubSub secondPubSub =
                new CustomBrokerUriPubSub(URI.create(secondBroker
                        .getConnectString()));
        assertThat(secondBroker, is(notNullValue()));
        assertThat(secondPubSub, is(notNullValue()));

        createEventListener(secondPubSub, observedEvents, msgWaitLock);

        LOG.info("Sending message from new broker to new broker...");
        Event event = new EventOne();
        secondPubSub.publish(null, event);
        assertThat(msgWaitLock.tryAcquire(2, TimeUnit.SECONDS), is(true));
        assertThat(observedEvents, hasItem(event));

        msgWaitLock.drainPermits();
        observedEvents.clear();
        createEventListener(defaultPubSub, observedEvents, msgWaitLock);
        LOG.info("Sending message from new broker to default broker...");
        event = new EventOne();
        secondPubSub.publish(null, event);
        assertThat(msgWaitLock.tryAcquire(2, TimeUnit.SECONDS), is(true));
        assertThat(observedEvents, hasItem(event));

        msgWaitLock.drainPermits();
        observedEvents.clear();
        LOG.info("Sending message from default broker to new broker...");
        event = new EventOne();
        defaultPubSub.publish(null, event);
        assertThat(msgWaitLock.tryAcquire(2, TimeUnit.SECONDS), is(true));
        assertThat(observedEvents, hasItem(event));

        secondPubSub.destroy();
        secondBroker.destroy();
    }

    /**
     * Multiple brokers should be able to discover each other and pass messages
     * for consumers.
     * 
     * This should also work if the first message is sent from the old broker
     * and should arrive at the new one.
     * 
     * @throws Exception
     */
    @Test
    public void testBrokerDiscoveryOtherWay() throws Exception {
        MessageBroker secondBroker = new MessageBroker(nameProvider);
        LOG.info("Second broker connect string: {}",
                secondBroker.getConnectString());
        PubSub secondPubSub =
                new CustomBrokerUriPubSub(URI.create(secondBroker
                        .getConnectString()));
        assertThat(secondBroker, is(notNullValue()));
        assertThat(secondPubSub, is(notNullValue()));

        createEventListener(secondPubSub, observedEvents, msgWaitLock);

        LOG.info("Sending message from new broker to new broker...");
        Event event = new EventOne();
        secondPubSub.publish(null, event);
        assertThat(msgWaitLock.tryAcquire(2, TimeUnit.SECONDS), is(true));
        assertThat(observedEvents, hasItem(event));

        msgWaitLock.drainPermits();
        observedEvents.clear();
        LOG.info("Sending message from default broker to new broker...");
        event = new EventOne();
        defaultPubSub.publish(null, event);
        assertThat(msgWaitLock.tryAcquire(2, TimeUnit.SECONDS), is(true));
        assertThat(observedEvents, hasItem(event));

        msgWaitLock.drainPermits();
        observedEvents.clear();
        createEventListener(defaultPubSub, observedEvents, msgWaitLock);
        LOG.info("Sending message from new broker to default broker...");
        event = new EventOne();
        secondPubSub.publish(null, event);
        assertThat(msgWaitLock.tryAcquire(2, TimeUnit.SECONDS), is(true));
        assertThat(observedEvents, hasItem(event));

        secondPubSub.destroy();
        secondBroker.destroy();
    }
}
