/**
 * 
 */
package eu.indenica.monitoring;

import eu.indenica.common.EventListener;
import eu.indenica.common.RuntimeComponent;

/**
 * The monitoring engine executes monitoring queries ({@link MonitoringQuery}),
 * and allows for starting and stopping them.
 * 
 * @author Christian Inzinger
 */
public interface MonitoringEngine extends RuntimeComponent, EventListener {
    static String SERVICE_NAME = "monitoring";

    /**
     * Add a {@link MonitoringQuery} to the monitoring engine and start it.
     * 
     * @param query
     *            the {@link MonitoringQuery} to add
     */
    void addQuery(MonitoringQuery query);

    /**
     * Start a monitoring query with the given name
     * 
     * @param queryName
     *            the name of the query to start
     * @throws IllegalArgumentException
     *             if no query with the given name exists
     */
    void startQuery(String queryName) throws IllegalArgumentException;

    /**
     * Stop monitoring query with the given name
     * 
     * @param queryName
     *            the name of the query to stop
     * @throws IllegalArgumentException
     *             if no query with the given name exists
     */
    void stopQuery(String queryName) throws IllegalArgumentException;
}
