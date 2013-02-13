/**
 * 
 */
package eu.indenica.runtime;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;

import org.apache.tuscany.sca.host.embedded.SCADomain;
import org.slf4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.indenica.common.LoggerFactory;
import eu.indenica.common.PubSubFactory;
import eu.indenica.integration.PlatformAdapter;
import eu.indenica.messaging.MessageBroker;

/**
 * This is the runtime entry point and will start an Indenica runtime instance
 * with the supplied configuration.
 * 
 * Manages SCA components ({@link PlatformAdapter}s) as well as auxiliary
 * services, such as messaging fabric and control infrastructure.
 * 
 * @author Christian Inzinger
 * 
 */
public class Launch {
	private final static Logger LOG = LoggerFactory.getLogger();

	@SuppressWarnings("serial")
	private static Properties config = new Properties(new Properties() {
		{
			// Fallback default Configuration
			setProperty("compositeFilename", System.getProperty(
					"indenica.runtimeCompositeFilename", "runtime.composite"));
		}
	}) {
		{
			try {
				// load default config.
				InputStream propIn =
						getClass().getResourceAsStream("/runtime.properties");
				load(propIn);
				propIn.close();
			} catch(Exception e) {
				LOG.debug("Could not load default configuration, "
						+ "falling back to factory defaults!");
			}

		}
	};

	private MessageBroker messageBroker;

	/**
	 * Known SCA domains
	 */
	private Collection<SCADomain> domains;

	/**
	 * Creates a runtime instance and starts auxiliary components.
	 * 
	 * @throws Exception
	 *             if something goes wrong
	 */
	public Launch() throws Exception {
		domains = Lists.newArrayList();
		domains = Collections.synchronizedCollection(domains);
		messageBroker = new MessageBroker();
	}

	/**
	 * Loads a contribution from the specified composite file.
	 * 
	 * @param composite
	 *            the composite to load
	 */
	public void loadContribution(String composite) {
		LOG.info("Adding contribution {}...", composite);
		SCADomain domain = SCADomain.newInstance(composite);

		// Tuscany 2
		// String contribution =
		// ContributionLocationHelper
		// .getContributionLocation(Launch.class);
		// Node node =
		// NodeFactory.newInstance().createNode(configFilename,
		// new Contribution("test", contribution));
		// node.start();

		LOG.debug("Node for {} started.", composite);
		domains.add(domain);
	}

	/**
	 * Returns the names of all known components.
	 * 
	 * @return the names of all known components
	 */
	public Set<String> getComponentNames() {
		Set<String> result = Sets.newHashSet();
		synchronized(domains) {
			for(SCADomain domain : domains)
				result.addAll(domain.getComponentManager().getComponentNames());
		}
		return result;
	}

	/**
	 * Returns a proxy for a service provided by a loaded SCA component.
	 * 
	 * @see SCADomain#getService(Class, String)
	 * @param businessInterface
	 *            the interface used to access the service
	 * @param serviceName
	 *            the service name
	 * @return a proxy object implementing the supplied interface
	 */
	public <T> T getService(Class<? extends T> businessInterface,
			String serviceName) {
		synchronized(domains) {
			for(SCADomain domain : domains) {
				if(domain.getComponentManager().getComponentNames()
						.contains(serviceName))
					return domain.getService(businessInterface, serviceName);
			}
		}
		LOG.warn("Service '{}' not found.", serviceName);
		return null;
	}

	/**
	 * Stops the runtime.
	 * 
	 * @throws Exception
	 *             if something goes wrong
	 */
	public void destroy() throws Exception {
		synchronized(domains) {
			for(SCADomain domain : domains)
				domain.close();
		}

		PubSubFactory.getPubSub().destroy();
		messageBroker.destroy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		destroy();
		super.finalize();
	}

	public static void main(String[] args) throws Exception {
		String configFilename = config.getProperty("compositeFilename");
		LOG.debug("Starting domain for composite '{}'...", configFilename);

		Launch runtime = new Launch();
		runtime.loadContribution(configFilename);

		LOG.info("Domain for composite '{}' started. Press any key to quit.",
				configFilename);

		System.in.read();
		LOG.info("Shutting down runtime...");

		runtime.destroy();
		LOG.debug("Shutdown complete.");
	}
}
