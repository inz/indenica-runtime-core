/**
 * 
 */
package eu.indenica.events;

/**
 * Sample event
 * 
 * @author Christian Inzinger
 */
public class EventOne extends Event {
    private static final long serialVersionUID = 3402115269434670223L;
    private static final long typeId = System.currentTimeMillis();
    private String message;

    public EventOne() {
        super("test.EventOne." + typeId);
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}