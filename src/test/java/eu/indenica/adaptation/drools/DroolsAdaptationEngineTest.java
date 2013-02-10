/**
 * 
 */
package eu.indenica.adaptation.drools;

import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.indenica.adaptation.AdaptationRuleImpl;
import eu.indenica.messaging.MessageBroker;

/**
 * @author Christian Inzinger
 * 
 */
public class DroolsAdaptationEngineTest {
	static MessageBroker broker = null;

	@BeforeClass
	public static void setup() throws Exception {
		broker = new MessageBroker();
	}

	@AfterClass
	public static void teardown() throws Exception {
		if(broker != null)
			broker.destroy();
	}

	@Test
	public void testRuleFires() throws Exception {
		DroolsAdaptationEngine engine = new DroolsAdaptationEngine();
		AdaptationRuleImpl ruleOne = new AdaptationRuleImpl();
		ruleOne.setStatement("global eu.indenica.adaptation.AdaptationEngine publisher;"
				+ "rule Simple when eval(true) "
				+ "then System.out.println(\"rule fired!\"); "
				+ "org.junit.Assert.assertTrue(true); end");
		engine.setRules(new AdaptationRuleImpl[] { ruleOne });
		engine.init();
		// engine.eventReceived(null, new EventOne());
		assertTrue(true);
		Thread.sleep(1000);
		engine.destroy();
		assertTrue(true);
	}

}
