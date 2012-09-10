/**
 * 
 */
package eu.indenica.events;

import eu.indenica.integration.PlatformAdapter;
import eu.indenica.monitoring.MonitoringQuery;

/**
 * This class represents monitoring events emitted by integrated platforms
 * through their {@link PlatformAdapter}s and/or {@link MonitoringQuery}s.
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

	protected String eventType;
	
	public Event() { }
	
	public Event(String eventType) {
		this.eventType = eventType;
	}
	
	public String getEventType() {
		return eventType;
	}
	
	/**
	 * @param eventType the eventType to set
	 */
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result =
				prime * result
						+ ((eventType == null) ? 0 : eventType.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(!(obj instanceof Event))
			return false;
		Event other = (Event) obj;
		if(eventType == null) {
			if(other.eventType != null)
				return false;
		} else if(!eventType.equals(other.eventType))
			return false;
		return true;
	}
}
