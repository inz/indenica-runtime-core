/**
 * 
 */
package eu.indenica.integration;

import eu.indenica.adaptation.Action;

/**
 * @author Christian Inzinger
 *
 */
public interface AdaptationInterface {
	void performAction(Action action);
	void registerCallback();
}
