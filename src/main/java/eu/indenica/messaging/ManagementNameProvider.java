/**
 * 
 */
package eu.indenica.messaging;

import com.google.common.base.Joiner;

/**
 * This class provides convenience methods for creating topic and queue names
 * for the management infrastructure.
 * 
 * @author Christian Inzinger
 */
public class ManagementNameProvider {
    /**
     * Common Prefix for management infrastructure.
     */
    public static final String MANAGEMENT_PREFIX = "management";

    private static final Joiner joiner = Joiner.on(".");

    /**
     * Returns a name in the management namespace for the given topic.
     * 
     * @param topic
     *            the topic name
     * @return the topic name in the management namespace
     */
    public static String getManagementTopicName(String topic) {
        return joiner.join(MANAGEMENT_PREFIX, topic);
    }

    /**
     * Returns a queue name in the management namespace for the given service
     * and node.
     * 
     * @param nodeName
     *            the node name
     * @param serviceName
     *            the service name
     * @return a queue name in the management namespace for the given service
     *         and node
     */
    public static String getServiceManagementQueueName(String nodeName,
            String serviceName) {
        return joiner.join(MANAGEMENT_PREFIX, nodeName, serviceName);
    }
}
