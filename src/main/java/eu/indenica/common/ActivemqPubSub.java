/**
 * 
 */
package eu.indenica.common;

import java.net.URI;
import java.util.Collection;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.osoa.sca.annotations.Init;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

import eu.indenica.events.Event;
import eu.indenica.messaging.MessageBroker;

/**
 * Messaging fabric accessing JMS/ActiveMQ embedded brokers and multicast
 * discovery to deliver messages in a distributed deployment.
 * 
 * @author Christian Inzinger
 * 
 */
public class ActivemqPubSub implements PubSub, EventListener {
	private final static Logger LOG = LoggerFactory.getLogger();
	private final static URI brokerUri = URI.create("discovery:("
			+ MessageBroker.discoveryUri.toString() + ")");
	// private final static URI brokerUri = URI.create("vm://localhost");
	private Connection connection;

	/**
	 * @throws Exception
	 *             when the broker fails to start.
	 * 
	 */
	public ActivemqPubSub() throws Exception {
		ActiveMQConnectionFactory connectionFactory =
				new ActiveMQConnectionFactory(brokerUri);
		connection = connectionFactory.createConnection();
		connection.start();
	}

	@Init
	public void init() throws JMSException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.indenica.common.PubSub#publish(eu.indenica.common.RuntimeComponent,
	 * eu.indenica.events.Event)
	 */
	@Override
	public void publish(final RuntimeComponent source, final Event event) {
		Collection<String> topicNames = Lists.newArrayList();
		if(source != null) {
			// FIXME: RuntimeComponents should have name to act as event source
			topicNames.add(":" + source.getClass().getCanonicalName() + ":"
					+ event.getEventType());
		}
		// Topic for event type (independent of source)
		topicNames.add("::" + event.getEventType());
		try {
			Session session =
					connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			Message message = session.createObjectMessage(event);

			MessageProducer producer = session.createProducer(null);
			for(String topicName : topicNames) {
				LOG.trace("Sending {} to topic {}", message, topicName);
				producer.send(session.createTopic(topicName), message);
			}
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
			final RuntimeComponent source, final Event event) {
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
			final RuntimeComponent source, final String eventType) {
		String topicName = ":";

		/**
		 * FIXME: refactor! Use canonical interface to get source name, event
		 * type name, i.e., topic name
		 */
		if(source != null)
			topicName += source.getClass().getCanonicalName();
		topicName += ":" + eventType;

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
						RuntimeComponent source = null;
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
	 * @see eu.indenica.common.PubSub#destroy()
	 */
	@Override
	public void destroy() throws InterruptedException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.indenica.common.EventListener#eventReceived(eu.indenica.common.
	 * RuntimeComponent, eu.indenica.events.Event)
	 */
	@Override
	public void eventReceived(final RuntimeComponent source, final Event event) {
		publish(source, event);
	}

	private static ActivemqPubSub instance = null;

	/**
	 * FIXME: factor out broker management into its own class!
	 * 
	 * @return An instance of the messaging fabric.
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

}
