/**
 * 
 */
package eu.indenica.common;


/**
 * @author Christian Inzinger
 *
 */
public interface RuntimeComponent {
	void init() throws Exception;
	void destroy() throws Exception;
}
