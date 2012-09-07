/**
 * 
 */
package eu.indenica.adaptation;

import eu.indenica.common.EventListener;
import eu.indenica.common.RuntimeComponent;

/**
 * @author Christian Inzinger
 *
 */
public interface AdaptationEngine extends RuntimeComponent, EventListener {
	void addRule(AdaptationRule rule);
	void setFact(Fact fact);
}
