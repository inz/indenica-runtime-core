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

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Fact [event=");
        if(this.equals(event)) {
            builder.append("self");
        } else {
            builder.append(event);
        }
        builder.append(" ").append(super.toString()).append("]");
        return builder.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((event == null || event == this) ? 0 : event.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(!super.equals(obj))
            return false;
        if(!(obj instanceof Fact))
            return false;
        Fact other = (Fact) obj;
        if(event == null) {
            if(other.event != null)
                return false;
        } else if(!event.equals(other.event))
            return false;
        return true;
    }
}
