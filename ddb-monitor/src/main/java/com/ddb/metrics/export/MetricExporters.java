package com.ddb.metrics.export;

import com.ddb.metrics.writer.GaugeWriter;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.HashMap;
import java.util.Map;

/**
 * @author evan
 * @Date 2015年12月15日T10:19
 */
public class MetricExporters implements SchedulingConfigurer {

    private Map<String, GaugeWriter> writers = new HashMap<String, GaugeWriter>();

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {

    }

}
