/**
 * 
 */
package eu.indenica.messaging;

import java.net.URI;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;

import eu.indenica.common.LoggerFactory;

/**
 * The management infrastructure client allows for components to receive control
 * messages during runtime. This management overlay only provides the means for
 * communication, the components are responsible for their own management logic.
 * 
 * @author Christian Inzinger
 */
public class ManagementClient {
    private static final Logger LOG = LoggerFactory.getLogger();

    private final String nodeName;
    private final String serviceName;
    private final Connection connection;

    /**
     * Creates a management infrastructure client for the given service and node
     * node names connecting to the default broker URI.
     * 
     * <p>
     * <b>Note:</b> In most cases, this is what you want. If, however, you want
     * to connect to a different broker, you can use
     * {@link #ManagementClient(URI)}.
     * 
     * @param serviceName
     *            the service name
     * @param nodeName
     *            the node name
     * @throws JMSException
     *             if something goes wrong
     */
    public ManagementClient(String nodeName, String serviceName)
            throws JMSException {
        this(nodeName, serviceName, DiscoveryNameProvider.DEFAULT_BROKER_URI);
    }

    /**
     * Creates a management infrastructure instance connecting to the supplied
     * broker URI
     * 
     * @param serviceName
     *            the service name
     * @param nodeName
     *            the node name
     * @param brokerUri
     *            the {@link URI} of the broker to connect to
     * @throws JMSException
     *             if something goes wrong
     */
    public ManagementClient(String nodeName, String serviceName, URI brokerUri)
            throws JMSException {
        this.nodeName = nodeName;
        this.serviceName = serviceName;
        LOG.info("Connecting to {}...", brokerUri);
        ActiveMQConnectionFactory connectionFactory =
                new ActiveMQConnectionFactory(brokerUri);
        connection = connectionFactory.createConnection();
        connection.start();
    }

    /**
     * Register a service management listener.
     * 
     * @param listener
     *            the listener to register
     * @throws JMSException
     *             if something goes wrong
     */
    public void registerListener(MessageListener listener) throws JMSException {
        registerListener(listener,
                ManagementNameProvider.getServiceManagementQueueName(nodeName,
                        serviceName));
    }

    /**
     * Register a broadcast listener for the given topic name
     * 
     * @param listener
     *            the listener to register
     * @param topic
     *            the topic name to listen to
     * @throws JMSException
     *             if something goes wrong
     */
    public void
            registerBroadcastListener(MessageListener listener, String topic)
                    throws JMSException {
        registerListener(listener,
                ManagementNameProvider.getManagementTopicName(topic));
    }

    /**
     * Register a listener with the given topic
     * 
     * @param listener
     *            the listener to register
     * @param topicName
     *            the topic name to listen to
     * @throws JMSException
     *             if something goes wrong
     */
    private void registerListener(MessageListener listener, String topicName)
            throws JMSException {
        Session session =
                connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer consumer =
                session.createConsumer(session.createTopic(topicName));
        consumer.setMessageListener(listener);
    }

    /**
     * Returns a new message
     * 
     * @return a new {@link MapMessage}
     * @throws JMSException
     *             if something goes wrong
     */
    public Message createMapMessage() throws JMSException {
        return connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
                .createMapMessage();
    }

    /**
     * Sends a message to the specified service on the specified node
     * 
     * @param message
     *            the message to send
     * @param nodeName
     *            the node to receive the message
     * @param serviceName
     *            the service to receive the message
     * @throws JMSException
     *             if something goes wrong
     */
    public void
            sendCommand(Message message, String nodeName, String serviceName)
                    throws JMSException {
        sendMessage(message,
                ManagementNameProvider.getServiceManagementQueueName(nodeName,
                        serviceName));
    }

    /**
     * Sends a message to the specified broadcast topic
     * 
     * @param message
     *            the message to send
     * @param topic
     *            the topic to send the message to
     * @throws JMSException
     *             if something goes wrong
     */
    public void sendBroadcast(Message message, String topic)
            throws JMSException {
        sendMessage(message,
                ManagementNameProvider.getManagementTopicName(topic));
    }

    /**
     * Sends the given management message to the specified topic.
     * 
     * @param message
     *            the message to send
     * @param topic
     *            the topic to send the message to
     * @throws JMSException
     *             if something goes wrong
     */
    private void sendMessage(Message message, String topic) throws JMSException {
        Session session =
                connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageProducer producer =
                session.createProducer(session.createTopic(topic));
        producer.send(message);
    }

    /**
     * Stop all message listeners and close the connection to the messaging
     * fabric.
     * 
     * @throws JMSException
     */
    public void stop() throws JMSException {
        connection.close();
    }
}
