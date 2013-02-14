/**
 * 
 */
package eu.indenica.adaptation.drools;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.indenica.adaptation.AdaptationRuleImpl;
import eu.indenica.messaging.MessageBroker;

/**
 * @author Christian Inzinger
 * 
 */
public class DroolsAdaptationEngineTest {
    private static MessageBroker broker = null;
    private Semaphore ruleFiredLock;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        broker = new MessageBroker();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        if(broker != null)
            broker.destroy();
    }

    @Before
    public void setUp() {
        ruleFiredLock = new Semaphore(0);
    }

    @After
    public void tearDown() {
        ruleFiredLock.drainPermits();
    }

    @Test
    public void testRuleFires() throws Exception {
        DroolsAdaptationEngine engine = new DroolsAdaptationEngine();
        engine.setGlobal("ruleFiredLock", ruleFiredLock);
        AdaptationRuleImpl ruleOne = new AdaptationRuleImpl();
        ruleOne.setStatement("global eu.indenica.adaptation.AdaptationEngine publisher;"
                + "global java.util.concurrent.Semaphore ruleFiredLock;"
                + "rule Simple when eval(true) "
                + "then eu.indenica.common.LoggerFactory.getLogger().info(\"fired!\"); "
                + "ruleFiredLock.release();" + " end");
        engine.setRules(new AdaptationRuleImpl[] { ruleOne });
        engine.init();
        assertThat(ruleFiredLock.tryAcquire(2, TimeUnit.SECONDS), is(true));
        engine.destroy();
    }

}
