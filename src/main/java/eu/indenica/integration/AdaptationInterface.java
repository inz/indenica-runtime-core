/**
 * 
 */
package eu.indenica.integration;

import org.osoa.sca.annotations.Remotable;

import eu.indenica.adaptation.Action;

/**
 * @author Christian Inzinger
 * 
 */
@Remotable
public interface AdaptationInterface {
    void performAction(Action action);

    void registerCallback();
}
