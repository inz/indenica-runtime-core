/**
 * 
 */
package eu.indenica.common;

import java.util.Collection;
import java.util.concurrent.Semaphore;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import eu.indenica.events.Event;

/**
 * @author Christian Inzinger
 * 
 */
public class TestUtils {
    private final static Logger LOG = (Logger) LoggerFactory.getLogger();

    /**
     * Set sensible logging levels, i.e. crank up our test logs, tone down
     * everything else.
     */
    public static void setLogLevels() {
        ((Logger) LoggerFactory.getLogger("root")).setLevel(Level.INFO);
        ((Logger) LoggerFactory.getLogger("eu.indenica")).setLevel(Level.TRACE);
    }
    
    /**
     * A sample Event
     * 
     * @author Christian Inzinger
     */
    public static class EventOne extends Event {
        private static final long serialVersionUID = 2114845083753269316L;
        private static final long typeId = System.currentTimeMillis();
        private String attr1;
        private int anAttribute;

        public EventOne() {
            super("test.EventOne." + typeId);
        }

        public void setAttr1(String attr1) {
            this.attr1 = attr1;
        }

        public void setAnAttribute(int anAttribute) {
            this.anAttribute = anAttribute;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return new StringBuilder().append("#<")
                    .append(getClass().getName()).append(": ")
                    .append("attr1: ").append(attr1).append(", anAttribute: ")
                    .append(anAttribute).append(">").toString();
        }
    }

    /**
     * Creates a default event listener for {@link EventOne}
     * 
     * @param pubSub
     *            the messaging fabric to use
     * @param observedEvents
     *            a collection to put received events in
     * @param msgWaitLock
     *            a semaphore to lock for external synchronization
     */
    public static void
            createEventListener(final PubSub pubSub,
                    final Collection<Event> observedEvents,
                    final Semaphore msgWaitLock) {
        createEventListener(pubSub, new EventOne().getEventType(),
                observedEvents, msgWaitLock);
    }

    /**
     * Creates an event listener for the specified event type.
     * 
     * @param pubSub
     *            the messaging fabric to use
     * @param eventType
     *            the event type to listen for
     * @param observedEvents
     *            a collection to put received events in
     * @param msgWaitLock
     *            a semaphore to lock for external synchronization
     */
    public static void createEventListener(final PubSub pubSub,
            final String eventType, final Collection<Event> observedEvents,
            final Semaphore msgWaitLock) {
        createEventListener(pubSub, null, eventType, observedEvents,
                msgWaitLock);
    }

    /**
     * Creates an event listener for the specified event type.
     * 
     * @param pubSub
     *            the messaging fabric to use
     * @param source
     *            the component name to listen for
     * @param eventType
     *            the event type to listen for
     * @param observedEvents
     *            a collection to put received events in
     * @param msgWaitLock
     *            a semaphore to lock for external synchronization
     */
    public static void
            createEventListener(final PubSub pubSub, final String source,
                    final String eventType,
                    final Collection<Event> observedEvents,
                    final Semaphore msgWaitLock) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        final String caller =
                stackTrace[stackTrace[2].getMethodName().startsWith(
                        "createEvent") ? 3 : 2].getMethodName();
        pubSub.registerListener(new EventListener() {
            @Override
            public void eventReceived(String source, Event event) {
                LOG.debug("{} - Received event {} in {} from {}", new Object[] {
                        caller, event, pubSub, source });
                observedEvents.add(event);
                msgWaitLock.release();
            }
        }, source, eventType);
    }
}
