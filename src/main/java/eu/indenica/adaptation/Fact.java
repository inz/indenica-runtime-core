/**
 * 
 */
package eu.indenica.adaptation;

import eu.indenica.events.Event;

/**
 * @author Christian Inzinger
 * 
 */
public class Fact extends Event {
    private static final long serialVersionUID = -96829167807110836L;
    protected Event event;

    public Fact(String factType) {
        super(factType);
    }

    /**
     * @return the event
     */
    public Event getEvent() {
        return event;
    }

    /**
     * @param event
     *            the event to set
     */
    public void setEvent(Event event) {
        this.event = event;
    }

    /**
     * @return the partitionKey
     */
    public Object getPartitionKey() {
        throw new UnsupportedOperationException();
    }

}
