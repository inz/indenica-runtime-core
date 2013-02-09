/**
 * 
 */
package eu.indenica.common;

import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.network.NetworkConnector;
import org.apache.activemq.transport.TransportFactory;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Scope;
import org.slf4j.Logger;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import eu.indenica.events.Event;

/**
 * @author Christian Inzinger
 * 
 */
@EagerInit
@Scope("COMPOSITE")
public class ActivemqPubSub implements PubSub {
	private final static Logger LOG = LoggerFactory.getLogger();
	private BrokerService broker;
	private ExecutorService notifierPool;
	private String hostname;
	private int port;

	/**
	 * @throws Exception
	 *             when the broker fails to start.
	 * 
	 */
	public ActivemqPubSub() throws Exception {
		notifierPool = Executors.newCachedThreadPool();

		broker = new BrokerService();
		broker.setPersistent(false);

		TransportConnector connector = new TransportConnector();
		connector.setUri(new URI("tcp://" + getHostname() + ":" + getPort()));
		connector.setDiscoveryUri(URI.create("multicast://default"
				+ "?group=indenica.internal.messaging"));
		broker.addConnector(connector);

		broker.start();
	}

	@Init
	public void init() {
	}

	/**
	 * @return the hostname
	 */
	public String getHostname() {
		if(hostname == null)
			return "localhost";
		return hostname;
	}

	/**
	 * @param hostname
	 *            the hostname to set
	 */
	@Property
	public void setHostname(String hostname) {
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
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @param newPeers
	 *            the peers to set
	 */
	@Property
	public void setPeers(String[] newPeers) {
		for(String newPeer : newPeers)
			connectPeer(newPeer);
	}

	private void connectPeer(String peerAddress) {
		try {
			LOG.info("Connecting to new peer: {}", peerAddress);
			broker.addNetworkConnector("static://(" + peerAddress + ")");
		} catch(Exception e) {
			LOG.error("Failed to connect peer", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.indenica.common.PubSub#publish(eu.indenica.common.RuntimeComponent,
	 * eu.indenica.events.Event)
	 */
	@Override
	public void publish(RuntimeComponent source, Event event) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.indenica.common.PubSub#publishAll(eu.indenica.common.EventEmitter)
	 */
	@Override
	public void publishAll(EventEmitter source) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.indenica.common.PubSub#registerListener(eu.indenica.common.EventListener
	 * , eu.indenica.common.RuntimeComponent, eu.indenica.events.Event)
	 */
	@Override
	public void registerListener(EventListener listener,
			RuntimeComponent source, Event event) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * eu.indenica.common.PubSub#registerListener(eu.indenica.common.EventListener
	 * , eu.indenica.common.RuntimeComponent, java.lang.String)
	 */
	@Override
	public void registerListener(EventListener listener,
			RuntimeComponent source, String eventType) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.indenica.common.PubSub#destroy()
	 */
	@Override
	public void destroy() throws InterruptedException {
		notifierPool.shutdown();
		notifierPool.awaitTermination(2, TimeUnit.SECONDS);
	}

}
