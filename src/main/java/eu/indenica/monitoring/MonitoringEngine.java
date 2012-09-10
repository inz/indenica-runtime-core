/**
 * 
 */
package eu.indenica.monitoring;

import eu.indenica.common.EventListener;
import eu.indenica.common.RuntimeComponent;

/**
 * @author Christian Inzinger
 *
 */
public interface MonitoringEngine extends RuntimeComponent, EventListener {
	void addQuery(MonitoringQuery rule);
}
