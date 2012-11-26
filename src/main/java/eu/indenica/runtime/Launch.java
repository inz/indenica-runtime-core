/**
 * 
 */
package eu.indenica.runtime;

import java.io.InputStream;
import java.util.Properties;

// Tuscany 2
//import org.apache.tuscany.sca.node.Contribution;
//import org.apache.tuscany.sca.node.ContributionLocationHelper;
//import org.apache.tuscany.sca.node.Node;
//import org.apache.tuscany.sca.node.NodeFactory;
// Tuscany 1
import org.apache.tuscany.sca.host.embedded.SCADomain;

import org.slf4j.Logger;

import eu.indenica.common.LoggerFactory;
import eu.indenica.common.PubSubFactory;

/**
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

	public static void main(String[] args) throws Exception {
		String configFilename = config.getProperty("compositeFilename");
		LOG.debug("Starting domain for composite '{}'...", configFilename);

		// SCADomain is apparently only available in tuscany 1.x
		SCADomain scaDomain = SCADomain.newInstance(configFilename);

		// Tuscany 2
		// String contribution =
		// ContributionLocationHelper
		// .getContributionLocation(Launch.class);
		// Node node =
		// NodeFactory.newInstance().createNode(configFilename,
		// new Contribution("test", contribution));
		// node.start();

		LOG.info("Domain for composite '{}' started. Press any key to quit.",
				configFilename);

		System.in.read();
		LOG.info("Shutting down domain...");

		scaDomain.close();
		PubSubFactory.getPubSub().destroy();
		// node.stop();
		LOG.debug("Shutdown complete.");
	}
}
