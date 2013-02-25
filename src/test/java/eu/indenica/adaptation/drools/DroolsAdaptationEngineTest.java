/**
 * 
 */
package eu.indenica.adaptation.drools;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.jms.Message;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.indenica.adaptation.AdaptationEngine;
import eu.indenica.adaptation.AdaptationRuleImpl;
import eu.indenica.adaptation.Fact;
import eu.indenica.common.TestUtils;
import eu.indenica.events.EventOne;
import eu.indenica.messaging.DiscoveryNameProvider;
import eu.indenica.messaging.ManagementClient;
import eu.indenica.messaging.MessageBroker;

/**
 * Tests for {@link DroolsAdaptationEngine}
 * 
 * @author Christian Inzinger
 */
public class DroolsAdaptationEngineTest {
    private MessageBroker broker;
    private Semaphore ruleFiredLock;
    private AdaptationEngine adaptationEngine;
    private ManagementClient mgmtClient;
    private String nodeName;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        TestUtils.setLogLevels();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        broker =
                new MessageBroker(new DiscoveryNameProvider(getClass()
                        .getSimpleName() + "-" + System.currentTimeMillis()));
        nodeName = "test-node-" + System.currentTimeMillis();
        mgmtClient = new ManagementClient(nodeName, "test-mgmt");

        ruleFiredLock = new Semaphore(0);
        adaptationEngine = new DroolsAdaptationEngine();
        adaptationEngine.setHostName(nodeName);
        ((DroolsAdaptationEngine) adaptationEngine).setGlobal("ruleFiredLock",
                ruleFiredLock);
        adaptationEngine.init();
    }

    @After
    public void tearDown() throws Exception {
        adaptationEngine.destroy();
        assertThat(ruleFiredLock.drainPermits(), is(0));
        broker.destroy();
    }

    /**
     * A simple rule that does not depend on external events should fire as soon
     * as the adaptation engine is initialized.
     * 
     * @throws Exception
     */
    @Test
    public void testSimpleRuleFires() throws Exception {
        DroolsAdaptationEngine engine = new DroolsAdaptationEngine();
        engine.setGlobal("ruleFiredLock", ruleFiredLock);
        AdaptationRuleImpl ruleOne = new AdaptationRuleImpl();
        ruleOne.setStatement("global eu.indenica.adaptation.AdaptationEngine engine;"
                + "global java.util.concurrent.Semaphore ruleFiredLock;"
                + "rule SimpleAddedBefore when eval(true) "
                + "then eu.indenica.common.LoggerFactory.getLogger().info(\"fired!\"); "
                + "ruleFiredLock.release();" + " end");
        engine.setRules(new AdaptationRuleImpl[] { ruleOne });
        engine.init();
        assertThat(ruleFiredLock.tryAcquire(2, TimeUnit.SECONDS), is(true));
        engine.destroy();
    }

    /**
     * The engine should allow adding a rule after it is initialized. A rule
     * that does not depend on external facts should fire as soon as it was
     * added.
     * 
     * @throws Exception
     */
    @Test
    public void testAddSimpleRule() throws Exception {
        AdaptationRuleImpl ruleOne = new AdaptationRuleImpl();
        ruleOne.setStatement("global eu.indenica.adaptation.AdaptationEngine engine;"
                + "global java.util.concurrent.Semaphore ruleFiredLock;"
                + "rule SimpleAddedAfterStart when "
                + "then eu.indenica.common.LoggerFactory.getLogger().info(\"fired!\"); "
                + "ruleFiredLock.release();" + " end");
        adaptationEngine.addRule(ruleOne);
        assertThat(ruleFiredLock.tryAcquire(2, TimeUnit.SECONDS), is(true));
    }

    /**
     * A rule should be triggered when its condition is satisfied.
     * 
     * @throws Exception
     */
    @Test
    public void testAddSimpleRuleTriggered() throws Exception {
        AdaptationRuleImpl ruleOne = new AdaptationRuleImpl();
        ruleOne.setStatement("global eu.indenica.adaptation.AdaptationEngine engine;"
                + "global java.util.concurrent.Semaphore ruleFiredLock;"
                + "rule SimpleWithTrigger when $a : eu.indenica.adaptation.Fact(eventType == \"factOne\") "
                + "then eu.indenica.common.LoggerFactory.getLogger().info(\"got event {}\", $a); "
                + "ruleFiredLock.release();" + " end");
        adaptationEngine.addRule(ruleOne);
        Fact fact = new Fact("factOne");
        EventOne event = new EventOne();
        event.setAttr1("one");
        fact.setEvent(event);
        adaptationEngine.eventReceived(null, fact);
        assertThat(ruleFiredLock.tryAcquire(2, TimeUnit.SECONDS), is(true));
    }

    /**
     * The engine should allow adding a rule using the messaging fabric. A rule
     * that does not depend on external facts should fire as soon as it was
     * added.
     * 
     * @throws Exception
     */
    @Test
    public void testMgmtClientAddSimpleRule() throws Exception {
        {
            String statement = "global eu.indenica.adaptation.AdaptationEngine engine;"
                    + "global java.util.concurrent.Semaphore ruleFiredLock;"
                    + "rule SimpleViaMgmtClient when "
                    + "then eu.indenica.common.LoggerFactory.getLogger().info(\"fired!\"); "
                    + "ruleFiredLock.release();" + " end";
            Message message = mgmtClient.createMapMessage();
            message.setStringProperty("command", "addRule");
            message.setStringProperty("ruleStatement", statement);
            mgmtClient.sendCommand(message, nodeName,
                    AdaptationEngine.SERVICE_NAME);
        }

        assertThat(ruleFiredLock.tryAcquire(2, TimeUnit.SECONDS), is(true));
    }
}
