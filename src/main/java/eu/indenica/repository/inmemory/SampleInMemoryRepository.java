/**
 * 
 */
package eu.indenica.repository.inmemory;

import java.util.Collection;

import org.osoa.sca.annotations.Scope;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

import eu.indenica.common.LoggerFactory;
import eu.indenica.repository.AbstractRepository;
import eu.indenica.repository.Query;

/**
 * The in-memory repository is probably the most basic repository
 * implementation.
 * 
 * <p>
 * It cannot persistently store data, and queries can only be performed via the
 * {@link #queryEx(Query)} method, using a {@link Query} object that is matched
 * against every single object in the repository.
 * 
 * <p>
 * This repository implementation is mostly useful for unit testing and concept
 * demonstrations.
 * 
 * @author Christian Inzinger
 */
@Scope("COMPOSITE")
public class SampleInMemoryRepository extends AbstractRepository {
    private final static Logger LOG = LoggerFactory.getLogger();
    protected Collection<String> store = Lists.newArrayList();
    private String hostName;

    /*
     * (non-Javadoc)
     * 
     * @see eu.indenica.repository.Repository#query(java.lang.String)
     */
    @Override
    public String[] query(String query) {
        LOG.trace("Searching for '{}'", query);
        Collection<String> result = Lists.newArrayList();
        for(String s : store) {
            if(s.matches(query))
                result.add(s);
        }
        return result.toArray(new String[result.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.indenica.repository.Repository#store(java.lang.String)
     */
    @Override
    public void store(String jsonString) {
        LOG.trace("Storing '{}'", jsonString);
        store.add(jsonString);
    }

    /*
     * (non-Javadoc)
     * 
     * @see eu.indenica.repository.Repository#delete(java.lang.String)
     */
    @Override
    public void delete(String id) {
        store.remove(id);
    }

    /* (non-Javadoc)
     * @see eu.indenica.common.RuntimeComponent#setHostName(java.lang.String)
     */
    @Override
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

}
