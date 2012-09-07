/**
 * 
 */
package eu.indenica.monitoring;

import java.util.Collection;

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
 * monitoring rule. It consists of a platform {@link Event} type, an optional
 * {@link Filter}, and an optional {@link Window}</dd>
 * <dt>Tags</dt>
 * <dd>{@link Tag}s allow for grouping of {@link MonitoringRule}s into logical components.
 * </dd>
 * <dt>Output</dt>
 * <dd>A monitoring rule emits a stream of {@link Event}s.</dd>
 * </dl>
 * 
 * @author Christian Inzinger
 */
public class MonitoringRule  {
	protected Collection<Class<? extends Event>> outputEventTypes;
	protected Collection<StreamDefinition> sources;
	
	public static class StreamDefinition {
		protected Filter filter;
		protected Window window;
	}
	
	public static class Filter {
		
	}
	
	public static class Window {
		
	}
}
