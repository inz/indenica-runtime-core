/**
 * 
 */
package eu.indenica.common;

/**
 * The basic runtime element in the component infrastructure.
 * 
 * <p>
 * All runtime components implement this interface. Components should implement
 * a public default constructor and their properties initialized before calling
 * {@link #init()}.
 * 
 * @author Christian Inzinger
 */
public interface RuntimeComponent {
    /**
     * Initialize the component.
     * 
     * @throws Exception
     *             if something goes wrong
     */
    void init() throws Exception;

    /**
     * Destroy the component and free all allocated resources.
     * 
     * @throws Exception
     *             if something goes wrong
     */
    void destroy() throws Exception;

    /**
     * Set the host name to use.
     * 
     * <p>
     * The value of this property should default to the current machine's host
     * name, but can be overridden to allow for multiplie runtime instances on
     * the same machine.
     * 
     * @param hostName
     */
    void setHostName(String hostName);
}
