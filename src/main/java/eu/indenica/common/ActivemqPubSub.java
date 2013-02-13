/**
 * 
 */
package eu.indenica.common;

import java.net.URI;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;

import eu.indenica.events.Event;

/**
 * Messaging fabric accessing JMS/ActiveMQ embedded brokers and multicast
 * discovery to deliver messages in a distributed deployment.
 * 
 * @author Christian Inzinger
 * 
 */
public class ActivemqPubSub implements PubSub, EventListener {
    private final static Logger LOG = LoggerFactory.getLogger();
    public final static String baseTopic = "indenica.event";
    public final static String pathSeparator = ".";
    protected static URI defaultBrokerUri = URI
            .create("vm://localhost?create=false&waitForStart=2000");
    private final URI brokerUri;
    private final Connection connection;

    /**
     * Create and start the messaging infrastructure with default settings.
     * 
     * @throws Exception
     *             when the broker fails to start.
     * 
     */
    public ActivemqPubSub() throws Exception {
        this(defaultBrokerUri);
    }

    /**
     * Create and start the messaging infrastructure connecting to the specified
     * broker URI
     * 
     * @param brokerUri
     *            the {@link URI} of the broker to connect to
     * @throws Exception
     *             if something goes wrong
     */
    protected ActivemqPubSub(URI brokerUri) throws Exception {
        this.brokerUri = brokerUri;
        LOG.info("Connecting to {}...", brokerUri);
        ActiveMQConnectionFactory connectionFactory =
                new ActiveMQConnectionFactory(brokerUri);
        connection = connectionFactory.createConnection();
        connection.start();
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.indenica.common.PubSub#destroy()
     */
    @Override
    public void destroy() throws Exception {
        connection.close();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * eu.indenica.common.PubSub#publish(eu.indenica.common.RuntimeComponent,
     * eu.indenica.events.Event)
     */
    @Override
    public void publish(final String source, final Event event) {
        String topicName =
                new StringBuilder().append(baseTopic).append(pathSeparator)
                        .append(source == null ? "null" : source)
                        .append(pathSeparator).append(event.getEventType())
                        .toString();
        try {
            Session session =
                    connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Message message = session.createObjectMessage(event);

            MessageProducer producer = session.createProducer(null);
            LOG.trace("Sending {} to topic {}", event, topicName);
            producer.send(session.createTopic(topicName), message);
            session.close();
        } catch(JMSException e) {
            LOG.error("Something went wrong!", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * eu.indenica.common.PubSub#publishAll(eu.indenica.common.EventEmitter)
     */
    @Override
    public void publishAll(final EventEmitter source) {
        source.addEventListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * eu.indenica.common.PubSub#registerListener(eu.indenica.common.EventListener
     * , eu.indenica.common.RuntimeComponent, eu.indenica.events.Event)
     */
    @Override
    public void registerListener(final EventListener listener,
            final String source, final Event event) {
        registerListener(listener, source, event.getEventType());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * eu.indenica.common.PubSub#registerListener(eu.indenica.common.EventListener
     * , eu.indenica.common.RuntimeComponent, java.lang.String)
     */
    @Override
    public void registerListener(final EventListener listener,
            final String source, final String eventType) {
        String topicName =
                new StringBuilder().append(baseTopic).append(pathSeparator)
                        .append(source == null ? "*" : source)
                        .append(pathSeparator)
                        .append(eventType == null ? "*" : eventType).toString();

        try {
            LOG.info("Registering listener for {}...", topicName);
            Session session =
                    connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageConsumer consumer =
                    session.createConsumer(session.createTopic(topicName));
            consumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    if(!(message instanceof ObjectMessage)) {
                        LOG.error("Received unexpected message: {}", message);
                        throw new RuntimeException("Unexpected message!");
                    }

                    try {
                        // FIXME: get source component for event
                        String source =
                                ((Topic) message.getJMSDestination())
                                        .getTopicName();
                        Event receivedEvent =
                                (Event) ((ObjectMessage) message).getObject();
                        listener.eventReceived(source, receivedEvent);
                    } catch(JMSException e) {
                        LOG.error("Could not retrieve object from message", e);
                    }
                }
            });
        } catch(JMSException e) {
            LOG.error("Something went wrong!", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.indenica.common.EventListener#eventReceived(eu.indenica.common.
     * RuntimeComponent, eu.indenica.events.Event)
     */
    @Override
    public void eventReceived(final String source, final Event event) {
        publish(source, event);
    }

    private static ActivemqPubSub instance = null;

    /**
     * @return An instance of the messaging client fabric.
     */
    public static synchronized PubSub getInstance() {
        if(instance == null) {
            try {
                instance = new ActivemqPubSub();
            } catch(Exception e) {
                LOG.error("Error creating pubsub instance!", e);
            }
        }
        return instance;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new StringBuilder().append("#<").append(getClass().getName())
                .append(": ").append("defaultBrokerUri: ").append(brokerUri)
                .append(">").toString();
    }
}
