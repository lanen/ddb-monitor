package com.ddb.metrics.opentsdb;

/**
 * Strategy used to convert a metric name into an {@link OpenTsdbName}.
 *
 * @author Dave Syer
 * @since 1.3.0
 */
public interface OpenTsdbNamingStrategy {

    /**
     * Convert the metric name into a {@link OpenTsdbName}.
     * @param metricName the name of the metric
     * @return an Open TSDB name
     */
    OpenTsdbName getName(String metricName);

}