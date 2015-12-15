package com.ddb.metrics.falcon;

import com.codahale.metrics.MetricRegistry;
import org.junit.Test;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.boot.actuate.metrics.writer.DropwizardMetricWriter;
import org.springframework.context.annotation.Bean;

import static org.junit.Assert.*;

/**
 * @author evan
 * @Date 2015年12月15日T10:47
 */
public class FalconMetricWriterTest {

    @Bean
    public FalconMetricWriter metricWriter (FalconMetricProperties properties, MetricRegistry registry){
        FalconMetricWriter writer = new FalconMetricWriter(properties, registry);
        return writer;

    }


    @Test
    public void testWriter(){

        FalconMetricProperties properties = createFalconMetricProperties();

        MetricRegistry registry = new MetricRegistry();


        DropwizardMetricWriter metricWriter = new DropwizardMetricWriter(registry);

        Metric<Integer> integerMetric = new Metric<Integer>("hi",168);
        metricWriter.set(integerMetric);

        FalconMetricWriter writer = metricWriter(properties, registry);

        try {
            writer.afterPropertiesSet();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static FalconMetricProperties createFalconMetricProperties(){

        FalconMetricProperties properties = new FalconMetricProperties();

        FalconMetricProperties.Falcon f= new FalconMetricProperties.Falcon();

        properties.setFalcon(f);

        f.setEndpoint("testpoint");
        f.setUrl("http://localhost:1688/v1/push");
        f.setTags("hi=1");
        f.setStep(60);
        f.setReportTimeMs(1000);

        return properties;
    }
}
