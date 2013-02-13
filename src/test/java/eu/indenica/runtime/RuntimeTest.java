/**
 * 
 */
package eu.indenica.runtime;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.collection.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.tuscany.sca.host.embedded.SCADomain;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

import eu.indenica.adaptation.Action;
import eu.indenica.common.LoggerFactory;
import eu.indenica.events.ActionEvent;
import eu.indenica.repository.Repository;

/**
 * @author Christian Inzinger
 */
public class RuntimeTest {
	private final static Logger LOG = LoggerFactory.getLogger();
	// private static Node runtimeNode;
	private static Launch runtimeNode;

	@BeforeClass
	public static void setup() throws Exception {
		((ch.qos.logback.classic.Logger) LoggerFactory.getLogger("root"))
				.setLevel(ch.qos.logback.classic.Level.TRACE);
//		((ch.qos.logback.classic.Logger) LoggerFactory
//				.getLogger("org.apache.activemq"))
//				.setLevel(ch.qos.logback.classic.Level.INFO);

		// String runtimeLocation =
		// ContributionLocationHelper
		// .getContributionLocation("rt.composite");
		String runtimeLocation = "runtime.composite";

		// String clientLocation =
		// ContributionLocationHelper.getContributionLocation("client.composite");
		LOG.debug("Starting node w/ composite '{}'", runtimeLocation);
		// runtimeNode =
		// NodeFactory.newInstance().createNode(
		// new Contribution("runtime", runtimeLocation)
		// // , new Contribution("client", clientLocation);
		// );
		// runtimeNode.start();

		runtimeNode = new Launch();
		runtimeNode.loadContribution(runtimeLocation);
		LOG.debug("Node started ({}).", runtimeNode);
		LOG.info("Components: {}", runtimeNode.getComponentNames());
	}

	@AfterClass
	public static void teardown() throws Exception {
		LOG.debug("Stopping node...");
		if(runtimeNode != null)
			runtimeNode.destroy();
	}

	@Test
	public void testSerialize() throws Exception {
		JAXBContext context = JAXBContext.newInstance(ActionEvent.class);
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		m.marshal(new ActionEvent(new Action()), System.out);
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
