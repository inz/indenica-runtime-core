/**
 * 
 */
package eu.indenica.repository;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.tuscany.sca.databinding.jaxb.AnyTypeXmlAdapter;

/**
 * @author Christian Inzinger
 * 
 */
@XmlJavaTypeAdapter(AnyTypeXmlAdapter.class)
public interface Query<T> {
	/**
	 * Matches the given object against this query.
	 * 
	 * <p>
	 * Will return the object if it matches, or {@code null} otherwise.
	 * 
	 * @param objectToMatch
	 *            the object to match
	 * @return the matched object, or {@code null} otherwise
	 */
	T matchObject(Object objectToMatch);
}
