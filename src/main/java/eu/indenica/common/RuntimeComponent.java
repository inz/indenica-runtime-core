/**
 * 
 */
package eu.indenica.common;


/**
 * @author Christian Inzinger
 *
 */
public interface RuntimeComponent {
	void init(PubSub pubsub) throws Exception;
	void destroy() throws Exception;
}
