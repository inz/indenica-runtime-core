/**
 * 
 */
package eu.indenica.adaptation;

/**
 * @author Christian Inzinger
 *
 */
public class AdaptationRuleImpl implements AdaptationRule {
	private String statement;
	private String[] inputEventTypes;

	/* (non-Javadoc)
	 * @see eu.indenica.adaptation.AdaptationRule#getStatement()
	 */
	@Override
	public String getStatement() {
		return this.statement;
	}

	/**
	 * @param statement the statement to set
	 */
	public void setStatement(String statement) {
		this.statement = statement;
	}

	/* (non-Javadoc)
	 * @see eu.indenica.adaptation.AdaptationRule#getInputEventTypes()
	 */
	@Override
	public String[] getInputEventTypes() {
		return this.inputEventTypes;
	}
	
	/**
	 * @param inputEventTypes the inputEventTypes to set
	 */
	public void setInputEventTypes(String[] inputEventTypes) {
		this.inputEventTypes = inputEventTypes;
	}
}
