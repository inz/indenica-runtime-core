/**
 * 
 */
package eu.indenica.messaging;

import java.net.URI;
import java.net.UnknownHostException;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
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

	private BrokerService broker;
	private String hostname;
	private int port;

	/**
	 * @throws Exception
	 *             if the broker cannot be started
	 */
	public MessageBroker() throws Exception {
		LOG.info("Starting message broker...");
		broker = new BrokerService();
		broker.setPersistent(false);
		broker.getSystemUsage().getTempUsage().setLimit(1024000);

		TransportConnector connector = new TransportConnector();
		connector.setUri(new URI("tcp://" + getHostname() + ":" + getPort()));
		connector.setDiscoveryUri(discoveryUri);
		broker.addConnector(connector);
		broker.addConnector("vm://localhost");

		broker.start();

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
