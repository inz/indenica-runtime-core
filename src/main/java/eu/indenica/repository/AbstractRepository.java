/**
 * 
 */
package eu.indenica.repository;

// Tuscany 2:
//import org.oasisopen.sca.annotation.Destroy;
//import org.oasisopen.sca.annotation.Init;
// Tuscany 1:
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.Init;

import org.slf4j.Logger;

import eu.indenica.common.LoggerFactory;



/**
 * @author Christian Inzinger
 *
 */
public abstract class AbstractRepository implements Repository {
	private final static Logger LOG = LoggerFactory.getLogger();

	/* (non-Javadoc)
	 * @see eu.indenica.runtime.RuntimeComponent#init()
	 */
	@Init
	public void init() throws Exception {
		LOG.debug("Starting component {}", getClass());
	}

	/* (non-Javadoc)
	 * @see eu.indenica.runtime.RuntimeComponent#destroy()
	 */
	@Destroy
	public void destroy() throws Exception {
		LOG.debug("Stopping component {}", getClass());
	}
}
