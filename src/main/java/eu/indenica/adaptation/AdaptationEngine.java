/**
 * 
 */
package eu.indenica.adaptation;

import eu.indenica.common.EventListener;
import eu.indenica.common.RuntimeComponent;
import eu.indenica.events.ActionEvent;

/**
 * @author Christian Inzinger
 *
 */
public interface AdaptationEngine extends RuntimeComponent, EventListener {
	void addRule(AdaptationRule rule);
	void setFact(Fact fact);
	void setRules(String[] rules);
	void setInputEventTypes(String[] inputEventTypes);
	void performAction(ActionEvent event);
}
