package com.ddb.metrics.writer;

import org.springframework.boot.actuate.metrics.Metric;

/**
 * @author evan
 * @Date 2015年12月15日T00:22
 */
public interface GaugeWriter {

    /**
     * Set the value of a metric.
     * @param value the value
     */
    void set(Metric<?> value);

}
