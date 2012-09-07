/**
 * 
 */
package eu.indenica.monitoring;

import javax.xml.bind.annotation.XmlSeeAlso;

import eu.indenica.integration.PlatformAdapter;

/**
 * This class represents monitoring events emitted by integrated platforms
 * through their {@link PlatformAdapter}s and/or {@link MonitoringRule}s.
 * 
 * <p>
 * An event is a considered a POJO, so developers can easily define custom
 * events that suite their specific needs.
 * 
 * <p>
 * The repository will hold a collection of all known event types and can be
 * queried for them.
 * 
 * @author Christian Inzinger
 */
// @XmlSeeAlso()
public class Event {

	public static class ServiceStartedEvent {
		public long timestamp;
	}
}
