/**
 * 
 */
package eu.indenica.integration;

import org.osoa.sca.annotations.Remotable;

import eu.indenica.events.Event;

/**
 * @author Christian Inzinger
 * 
 */
@Remotable
public interface EventReceiver {
    void receiveEvent(Event event);
}
