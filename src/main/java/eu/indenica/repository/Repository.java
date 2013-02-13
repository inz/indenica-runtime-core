/**
 * 
 */
package eu.indenica.repository;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.osoa.sca.annotations.Remotable;

import eu.indenica.common.RuntimeComponent;

/**
 * @author Christian Inzinger
 */
@Remotable
public interface Repository extends RuntimeComponent {
    @GET
    @Path("{q}")
    String[] query(@PathParam("{q}") String query);

    @POST
    void store(String jsonString);

    @DELETE
    @Path("{id}")
    void delete(@PathParam("{id}") String id);
}
