/**
 * 
 */
package eu.indenica.runtime;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.collection.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

import org.apache.tuscany.sca.host.embedded.SCADomain;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

import eu.indenica.common.LoggerFactory;
import eu.indenica.repository.Repository;

/**
 * @author Christian Inzinger
 */
public class RuntimeTest {
	private final static Logger LOG = LoggerFactory.getLogger();
	// private static Node runtimeNode;
	private static SCADomain runtimeNode;

	@BeforeClass
	public static void setup() throws Exception {
		((ch.qos.logback.classic.Logger) LoggerFactory.getLogger("root"))
				.setLevel(ch.qos.logback.classic.Level.TRACE);

		// String runtimeLocation =
		// ContributionLocationHelper
		// .getContributionLocation("rt.composite");
		String runtimeLocation = "rt.composite";

		// String clientLocation =
		// ContributionLocationHelper.getContributionLocation("client.composite");
		LOG.debug("Starting node w/ composite '{}'", runtimeLocation);
		// runtimeNode =
		// NodeFactory.newInstance().createNode(
		// new Contribution("runtime", runtimeLocation)
		// // , new Contribution("client", clientLocation);
		// );
		// runtimeNode.start();

		runtimeNode = SCADomain.newInstance(runtimeLocation);
		LOG.debug("Node started ({}).", runtimeNode);
	}

	@AfterClass
	public static void teardown() throws Exception {
		LOG.debug("Stopping node...");
		if(runtimeNode != null)
			// runtimeNode.stop();
			runtimeNode.close();
	}

	@Test
	public void testServicesStarted() throws Exception {
		LOG.debug("Accessing repository...");
		// TODO: access repository via smth. else?
		Repository repository =
				runtimeNode.getService(Repository.class, "Repository");
		repository.store("test");
		assertThat(Lists.newArrayList(repository.query("test")),
				hasItem("test"));
		LOG.debug("repo: {}", repository);
		assertThat(repository, is(notNullValue()));
	}
}
