/**
 * 
 */
package eu.indenica.monitoring;

import javax.xml.bind.annotation.XmlRootElement;

import org.osoa.sca.annotations.Property;
import org.slf4j.Logger;

import eu.indenica.common.LoggerFactory;

/**
 * @author Christian Inzinger
 *
 */
@XmlRootElement
public class MonitoringQueryImpl implements MonitoringQuery {
	private final static Logger LOG = LoggerFactory.getLogger();
	
	protected String[] inputEventTypes;

	protected String[] outputEventTypes;
	
	protected String statement;
	
	/* (non-Javadoc)
	 * @see eu.indenica.monitoring.MonitoringQuery#getOutputEventTypes()
	 */
	@Override
	public String[] getOutputEventTypes() {
		return outputEventTypes;
	}
	
	/**
	 * @param outputEventTypes the outputEventTypes to set
	 */
	@Property
	public void setOutputEventTypes(String[] outputEventTypes) {
		this.outputEventTypes = outputEventTypes;
	}

	/* (non-Javadoc)
	 * @see eu.indenica.monitoring.MonitoringQuery#getInputEventTypes()
	 */
	@Override
	public String[] getInputEventTypes() {
		return inputEventTypes;
	}
	
	/**
	 * @param inputEventTypes the inputEventTypes to set
	 */
	@Property
	public void setInputEventTypes(String[] inputEventTypes) {
		LOG.info("set input event types to {}", inputEventTypes);
		this.inputEventTypes = inputEventTypes;
	}

	/* (non-Javadoc)
	 * @see eu.indenica.monitoring.MonitoringQuery#getStatement()
	 */
	@Override
	public String getStatement() {
		return statement;
	}

	/**
	 * @param statement the statement to set
	 */
	@Property
	public void setStatement(String statement) {
		this.statement = statement;
	}
}
