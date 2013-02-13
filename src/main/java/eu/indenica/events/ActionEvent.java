/**
 * 
 */
package eu.indenica.events;

import javax.xml.bind.annotation.XmlRootElement;

import eu.indenica.adaptation.Action;

/**
 * @author Christian Inzinger
 * 
 */
@XmlRootElement
public class ActionEvent extends Event {
    public static final String ACTION_EVENT_TYPE = "action";
    private final Action action;

    /**
     * @param eventType
     */
    public ActionEvent(final Action action) {
        super(ACTION_EVENT_TYPE);
        this.action = action;
    }

    /**
	 * 
	 */
    public ActionEvent() {
        action = null;
    }

    /**
     * @return the action
     */
    public Action getAction() {
        return action;
    }
}
