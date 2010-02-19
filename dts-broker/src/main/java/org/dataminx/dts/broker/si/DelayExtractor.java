/**
 *
 */
package org.dataminx.dts.broker.si;

/**
 * @author hnguyen
 *
 */
public interface DelayExtractor {

    /**
     * Extract delay information
     * @param object targeted object for extracting the delay information
     * @return delay period or date time formatted string
     */
    public String extractDelay(Object object);
}
