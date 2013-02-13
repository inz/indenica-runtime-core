/**
 * 
 */
package eu.indenica.adaptation;

import java.util.Arrays;

/**
 * @author Christian Inzinger
 * 
 */
public class AdaptationRuleImpl implements AdaptationRule {
    private String statement;
    private String[] inputEventTypes;

    /*
     * (non-Javadoc)
     * 
     * @see eu.indenica.adaptation.AdaptationRule#getStatement()
     */
    @Override
    public String getStatement() {
        return this.statement;
    }

    /**
     * @param statement
     *            the statement to set
     */
    public void setStatement(String statement) {
        this.statement = statement;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.indenica.adaptation.AdaptationRule#getInputEventTypes()
     */
    @Override
    public String[] getInputEventTypes() {
        return this.inputEventTypes;
    }

    /**
     * @param inputEventTypes
     *            the inputEventTypes to set
     */
    public void setInputEventTypes(String[] inputEventTypes) {
        this.inputEventTypes = inputEventTypes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder result =
                new StringBuilder().append("#<")
                        .append(getClass().getSimpleName()).append(":");
        if(getStatement() != null)
            result.append(" statement: '")
                    .append(getStatement().replaceAll("\\s+", " ").trim())
                    .append("'");

        if(getInputEventTypes() != null)
            result.append(", inputEventTypes: ").append(
                    Arrays.toString(getInputEventTypes()));

        result.append(">");
        return result.toString();
    }

}
