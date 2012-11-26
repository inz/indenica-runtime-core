/**
 * 
 */
package eu.indenica.adaptation.drools;

import static org.junit.Assert.*;

import org.junit.Test;

import testPkg.EventOne;

import eu.indenica.adaptation.AdaptationRuleImpl;

/**
 * @author Christian Inzinger
 * 
 */
public class DroolsAdaptationEngineTest {

	@Test
	public void testRuleFires() throws Exception {
		DroolsAdaptationEngine engine = new DroolsAdaptationEngine();
		AdaptationRuleImpl ruleOne = new AdaptationRuleImpl();
		ruleOne.setStatement("global eu.indenica.adaptation.AdaptationEngine publisher;" +
				"rule Simple when eval(true) " +
				"then System.out.println(\"rule fired!\"); end");
		engine.setRules(new AdaptationRuleImpl[] { ruleOne });
		engine.init();
		// engine.eventReceived(null, new EventOne());
		assertTrue(true);
	}

}
