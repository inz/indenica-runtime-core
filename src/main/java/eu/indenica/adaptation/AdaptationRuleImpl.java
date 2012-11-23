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
}
