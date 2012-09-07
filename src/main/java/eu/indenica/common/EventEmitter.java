/**
 * 
 */
package eu.indenica.common;

import eu.indenica.monitoring.Event;

/**
 * @author Christian Inzinger
 *
 */
public interface EventEmitter {
	void addEventListener(EventListener listener);
}
