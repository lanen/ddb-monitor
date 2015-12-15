package com.ddb.metrics.export;


/**
 * Generic interface for metric exports. As you scale up metric collection you will often
 * need to buffer metric data locally and export it periodically (e.g. for aggregation
 * across a cluster), so this is the marker interface for those operations. The trigger of
 * an export operation might be periodic or event driven, but it remains outside the scope
 * of this interface. You might for instance create an instance of an Exporter and trigger
 * it using a {@code @Scheduled} annotation in a Spring ApplicationContext.
 *
 * @author Dave Syer
 * @since 1.3.0
 */
public interface Exporter {

    /**
     * Export metric data.
     */
    void export();

}