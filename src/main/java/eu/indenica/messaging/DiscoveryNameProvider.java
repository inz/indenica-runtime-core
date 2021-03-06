/**
 * 
 */
package eu.indenica.messaging;

import java.net.URI;

import javax.annotation.Nonnull;

import com.google.common.base.Joiner;

/**
 * @author Christian Inzinger
 */
public final class DiscoveryNameProvider {
    /**
     * Default broker URI for clients to connect to. 
     */
    public static final URI DEFAULT_BROKER_URI = URI
            .create("vm://localhost?create=false&waitForStart=2000");
    
    /**
     * Prefix for multicast group names.
     */
    public static final String MCAST_GROUP_PREFIX = "indenica.internal";

    private static final Joiner nameJoiner = Joiner.on(".").skipNulls();

    private final String applicationName;

    /**
     * Creates an instance of the name provider for the given application
     * 
     * @param applicationName
     *            the application name
     */
    public DiscoveryNameProvider(@Nonnull final String applicationName) {
        if(applicationName.matches("[^a-z_\\-]")) {
            throw new IllegalArgumentException(
                    "Application name cannot contain spaces or special characters!");
        }

        this.applicationName = applicationName;
    }

    /**
     * Returns the multicast group name for components of the given application
     * to discover each other.
     * 
     * @param applicationName
     *            the application name to be used
     * @return the multicast group name
     */
    public String getMulticastGroupName() {
        return nameJoiner.join(MCAST_GROUP_PREFIX, applicationName);
    }

    /**
     * The broker network's discovery URI for the given application
     * 
     * @param applicationName
     *            the application name
     * @return the broker network discovery URI
     */
    public URI getMulticastGroupUri() {
        return URI.create("multicast://default?group="
                + getMulticastGroupName());
    }

    public URI getMulticastDiscoveryUri() {
        return URI.create("discovery:(" + getMulticastGroupUri().toString()
                + ")");
    }
}
