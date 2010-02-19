/**
 *
 */
package org.dataminx.dts.broker.si;

/**
 * Strategy interface for extracting delay transfer requirements from a job submission
 * message.
 *
 * @author hnguyen
 */
public interface DelayExtractor {

    /**
     * Extract delay information
     * @param object targeted object for extracting the delay information
     * @return delay period or date time formatted string
     */
    public String extractDelay(Object object);
}
