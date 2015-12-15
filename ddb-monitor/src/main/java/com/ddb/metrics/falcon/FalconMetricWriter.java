package com.ddb.metrics.falcon;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.actuate.metrics.writer.DropwizardMetricWriter;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * @author evan
 * @Date 2015年12月14日T21:28
 */
public class FalconMetricWriter implements InitializingBean {


    private static final Logger logger = LoggerFactory.getLogger(FalconMetricWriter.class);

    /**
     *
     */
    private FalconMetricProperties falconMetricProperties;

    /**
     *
     */
    private FalconOpenTsdbReporter reporter;

    /**
     *
     */
    private Falcon falcon;

    /**
     *
     * @param falconMetricProperties
     */
    public FalconMetricWriter(FalconMetricProperties falconMetricProperties,MetricRegistry metricRegistry) {

        this.falconMetricProperties = falconMetricProperties;
        setMetricRegistry(metricRegistry);
    }

    private MetricRegistry metricRegistry;

    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

    public void setMetricRegistry(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        if (null!=falconMetricProperties && null!=metricRegistry){

            logger.info("init falcon report endpoint#{},url#{},step#{},tag#{},time#{}",
                    falconMetricProperties.getFalcon().getEndpoint(),
                    falconMetricProperties.getFalcon().getUrl(),
                    falconMetricProperties.getFalcon().getStep(),
                    falconMetricProperties.getFalcon().getTags(),
                    falconMetricProperties.getFalcon().getReportTimeMs()
                    );

            reporter = FalconOpenTsdbReporter
                    .forRegistry(metricRegistry)
                    .falconMetricProperties(falconMetricProperties)
                    .prefixedWith("")
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .filter(MetricFilter.ALL)
                    .build();

            reporter.start(falconMetricProperties.getFalcon().getReportTimeMs(),TimeUnit.MILLISECONDS);
        }

    }
}
