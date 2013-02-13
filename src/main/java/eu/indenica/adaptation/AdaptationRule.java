/**
 * 
 */
package eu.indenica.adaptation;

/**
 * @author Christian Inzinger
 * 
 */
public interface AdaptationRule {
    String[] getInputEventTypes();

    String getStatement();
}
