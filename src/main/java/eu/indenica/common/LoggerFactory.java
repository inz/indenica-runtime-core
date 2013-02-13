/**
 * 
 */
package eu.indenica.common;

import org.slf4j.Logger;

/**
 * This class provides a convenience method for uniformly retrieving logger
 * instances for arbitrary classes.
 * 
 * <p>
 * Classes can define their logger instance by issuing: <code><pre>
 * public class Windmill {
 *     private static Logger LOG = LoggerFactory.getLogger();
 * }
 * </pre></code>
 * 
 * <p>
 * The wrapper class automatically retrieves the name of the calling class and
 * retrieves a {@link Logger} for it.
 * 
 * @author Christian Inzinger
 * 
 */
public class LoggerFactory {
    /**
     * Returns a logger named after the name of the calling class.
     * 
     * @return a logger named after the calling class.
     */
    public static Logger getLogger() {
        return getLogger(new Exception().getStackTrace()[1].getClassName());
    }

    /**
     * @see org.slf4j.LoggerFactory#getLogger(Class)
     * 
     * @param clazz
     *            the returned logger will be named after clazz
     * @return the logger
     */
    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    /**
     * @param name
     *            the name of the logger
     * @return the logger
     * @see org.slf4j.LoggerFactory#getLogger(String)
     */
    public static Logger getLogger(String name) {
        return org.slf4j.LoggerFactory.getLogger(name);
    }

    /**
     * Private constructor to prevent initialization.
     */
    private LoggerFactory() {
    }
}
