/**
 * 
 */
package eu.indenica.common;

import eu.indenica.monitoring.Event;

/**
 * @author Christian Inzinger
 *
 */
public interface EventListener extends java.util.EventListener {
	void processEvent(Event event);
}
