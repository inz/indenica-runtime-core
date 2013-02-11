/**
 * 
 */
package eu.indenica.messaging;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.broker.jmx.ManagementContext;
import org.apache.activemq.transport.vm.VMTransportFactory;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Scope;
import org.slf4j.Logger;

import eu.indenica.common.LoggerFactory;

/**
 * Messaging fabric using ActiveMQ embedded brokers and multicast discovery to
 * deliver messages in a distributed deployment.
 * 
 * @author Christian Inzinger
 * 
 */
@EagerInit
@Scope("COMPOSITE")
public class MessageBroker {
	private final static Logger LOG = LoggerFactory.getLogger();

	/**
	 * The name of the multicast group Indenica components will use to discover
	 * each other.
	 */
	public final static String mcastGroup = "indenica.internal.messaging";
	/**
	 * The broker network's discovery URI
	 */
	public final static URI discoveryUri = URI
			.create("multicast://default?group=" + mcastGroup);

	/**
	 * The name of the broker to bind with VM transport.
	 */
	public final static URI vmTransportUri = URI.create("vm://localhost");
	private BrokerService broker;
	private String hostname;
	private int port;
	private String connectString;

	/**
	 * Creates and starts the broker for the messaging fabric.
	 * 
	 * @throws Exception
	 *             if the broker cannot be started
	 */
	public MessageBroker() throws Exception {
		LOG.info("Starting message broker...");
		LOG.trace("This is broker {} in this VM",
				VMTransportFactory.SERVERS.size() + 1);
		broker = new BrokerService();
		setBrokerName(broker);
		broker.setPersistent(false);
		// broker.setUseJmx(false);
		broker.getSystemUsage().getTempUsage().setLimit(1024 * 1000); // 1000kB
		ManagementContext managementContext = broker.getManagementContext();

		/**
		 * Set unique free port for management connector in case there are
		 * multiple brokers running on one machine.
		 */
		managementContext.setConnectorPort(getFreePort());

		connectTcpTransport(broker);
		connectVmTransport(broker);

		broker.start();
		connectString = broker.getDefaultSocketURIString();


		LOG.info("Broker {} started.", broker.getBrokerName());
	}

	/**
	 * This method finds a free port on the machine.
	 * 
	 * <b>NOTE:</b> There is a possible race condition here!
	 * 
	 * @return a free port
	 */
	private int getFreePort() {
		int port = 1099 + VMTransportFactory.SERVERS.size();
		try {
			ServerSocket socket = new ServerSocket(0);
			port = socket.getLocalPort();
			socket.close();
		} catch(IOException e) {
			LOG.warn("Could not find free port, falling back to default.", e);
		}
		return port;
	}

	/**
	 * Connect the TCP transport for the given broker.
	 * 
	 * @param broker
	 *            the broker to be modified
	 * @return the broker, after modification
	 * @throws URISyntaxException
	 *             if hostname and/or port were invalid
	 * @throws Exception
	 *             if something goes wrong
	 */
	private BrokerService connectTcpTransport(BrokerService broker)
			throws URISyntaxException, Exception {
		TransportConnector connector = new TransportConnector();
		connector.setUri(new URI("tcp://" + getHostname() + ":" + getPort()));
		connector.setDiscoveryUri(discoveryUri);
		broker.addConnector(connector);
		return broker;
	}

	/**
	 * Connects the VM transport for the given broker.
	 * 
	 * The VM transport will only be added if no other broker in the current VM
	 * offers it.
	 * 
	 * @param broker
	 *            the broker to be modified
	 * @return the modified broker
	 * @throws Exception
	 *             if something goes wrong
	 */
	@SuppressWarnings("unused")
	private static BrokerService connectVmTransport(BrokerService broker)
			throws Exception {
		/**
		 * Only connect VM transport if we're the first broker in this VM
		 */
		if(!VMTransportFactory.SERVERS.containsKey(vmTransportUri.getHost())) {
			broker.addConnector(vmTransportUri);
		} else {
			LOG.warn("Could not bind VM transport. Is another broker running?");
		}
		return broker;
	}

	/**
	 * Sets unique name for this broker.
	 * 
	 * @param broker
	 *            the broker needing a name
	 * @return the broker
	 */
	private BrokerService setBrokerName(BrokerService broker) {
		StringBuilder brokerName = new StringBuilder().append(mcastGroup);
		brokerName.append(".").append(getHostname());
		brokerName.append(".").append(VMTransportFactory.SERVERS.size());
		/**
		 * FIXME: As long as it creates the persistent store (which it should
		 * not) giving a UUID as broker name will fill up your disk in 32MB
		 * increments. Also, broker names should be consistent across restarts.
		 */
		// brokerName.append(UUID.randomUUID().toString());
		broker.setBrokerName(brokerName.toString());
		return broker;
	}
	
	@Destroy
	public void destroy() throws Exception {
		LOG.debug("Shutting down message broker...");
		broker.stop();
		LOG.info("Message broker shut down.");
	}

	/**
	 * @return the hostname
	 */
	public String getHostname() {
		if(hostname == null) {
			try {
				return java.net.InetAddress.getLocalHost().getHostName();
			} catch(UnknownHostException e) {
				LOG.warn("Could not get host name for this machine!", e);
				return "localhost";
			}
		}
		return hostname;
	}

	/**
	 * @param hostname
	 *            the hostname to set
	 */
	@Property
	public void setHostname(final String hostname) {
		this.hostname = hostname;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	@Property
	public void setPort(final int port) {
		this.port = port;
	}

	/**
	 * @return the connectString
	 */
	public String getConnectString() {
		return connectString;
	}

	/**
	 * @param newPeers
	 *            the peers to set
	 */
	@Property
	public void setPeers(final String[] newPeers) {
		for(String newPeer : newPeers)
			connectPeer(newPeer);
	}

	private void connectPeer(final String peerAddress) {
		try {
			LOG.info("Connecting to new peer: {}", peerAddress);
			broker.addNetworkConnector("static://(" + peerAddress + ")");
		} catch(Exception e) {
			LOG.error("Failed to connect peer", e);
		}
	}
}
