/**
 * 
 */
package eu.indenica.monitoring;

/**
 * A monitoring rule is used to analyze monitoring data from underlying
 * platforms and/or other monitoring rules, emits enriched information, and is
 * executed in a {@link MonitoringEngine}.
 * 
 * <p>
 * Specifically, it encapsulates:
 * <dl>
 * <dt>Sources</dt>
 * <dd>An event source or stream definition specifies a (named) input of the
 * monitoring rule.</dd>
 * <dt>Output</dt>
 * <dd>A monitoring rule emits a stream of {@link eu.indenica.events.Event}s.</dd>
 * </dl>
 * 
 * @author Christian Inzinger
 */
public interface MonitoringQuery {
    String getName();

    String[] getOutputEventTypes();

    String[] getInputEventTypes();

    String getStatement();
}
