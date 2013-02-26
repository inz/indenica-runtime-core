/**
 * 
 */
package eu.indenica.facts;

import eu.indenica.adaptation.Fact;

/**
 * @author Christian Inzinger
 *
 */
public class FactOne extends Fact {
    private static final long serialVersionUID = 5160035493246838588L;
    private static final long typeId = System.currentTimeMillis();
    
    private String status;

    /**
     * @param factType
     */
    public FactOne() {
        super("test.FactOne." + typeId);
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }
    
    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }
}
