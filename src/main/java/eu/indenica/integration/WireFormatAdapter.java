/**
 * 
 */
package eu.indenica.integration;

import eu.indenica.adaptation.Action;

/**
 * @author Christian Inzinger
 * 
 */
public interface WireFormatAdapter {
    void performAction(String endpointAddress, Action action);
    // void performAction(ActionListener platform, Action action);
}
