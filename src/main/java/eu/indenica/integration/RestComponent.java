/**
 * 
 */
package eu.indenica.integration;

import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;

import eu.indenica.adaptation.Action;
import eu.indenica.common.LoggerFactory;

/**
 * @author Christian Inzinger
 * 
 */
public class RestComponent extends PlatformAdapter {
    private final static Logger LOG = LoggerFactory.getLogger();

    /*
     * (non-Javadoc)
     * 
     * @see
     * eu.indenica.common.ActionListener#performAction(eu.indenica.adaptation
     * .Action)
     */
    @Override
    public void performAction(Action action) {
        try {
            HttpURLConnection connection =
                    (HttpURLConnection) new URL(getURLFor(action))
                            .openConnection();
            connection.setRequestMethod(getRequestMethodFor(action));
            if(connection.getResponseCode() != 200) {
                LOG.info("Failed to perform action {} on component {}", action,
                        this);
            }
            connection.disconnect();
        } catch(Exception e) {
            LOG.error("Failed to perform action {} on component: {}", action, e);
        }
    }

    /**
     * @param action
     * @return
     */
    private String getRequestMethodFor(Action action) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param action
     * @return
     */
    private String getURLFor(Action action) {
        // TODO Auto-generated method stub
        return null;
    }

}
