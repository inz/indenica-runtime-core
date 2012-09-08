/**
 * 
 */
package eu.indenica.common;

import eu.indenica.events.Event;

/**
 * @author Christian Inzinger
 *
 */
public interface EventEmitter {
	void addEventListener(EventListener listener);
}
