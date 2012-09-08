/**
 * 
 */
package eu.indenica.common;

import eu.indenica.events.Event;

/**
 * @author Christian Inzinger
 *
 */
public interface EventListener extends java.util.EventListener {
	void publish(RuntimeComponent source, Event event);
}
