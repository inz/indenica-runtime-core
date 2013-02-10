/**
 * 
 */
package eu.indenica.common;

/**
 * @author Christian Inzinger
 *
 */
public class PubSubFactory {
	public static PubSub getPubSub() {
		return ActivemqPubSub.getInstance();
	}
}
