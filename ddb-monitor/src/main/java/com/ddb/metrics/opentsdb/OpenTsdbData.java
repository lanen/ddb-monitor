package com.ddb.metrics.opentsdb;

import java.util.Map;

/**
 * @author evan
 * @Date 2015年12月15日T00:20
 */
public class OpenTsdbData {

    private OpenTsdbName name;

    private Long timestamp;

    private Number value;

    private String endpoint;

    private String counterType;

    private int step;

    protected OpenTsdbData() {
        this.name = new OpenTsdbName();
    }

    public OpenTsdbData(String metric, Number value) {
        this(metric, value, System.currentTimeMillis());
    }

    public OpenTsdbData(String metric, Number value, Long timestamp) {
        this(new OpenTsdbName(metric), value, timestamp);
    }

    public OpenTsdbData(OpenTsdbName name, Number value, Long timestamp) {
        this.name = name;
        this.value = value;
        this.timestamp = timestamp;
    }

    public String getMetric() {
        return this.name.getMetric();
    }

    public void setMetric(String metric) {
        this.name.setMetric(metric);
    }

    public Long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Number getValue() {
        return this.value;
    }

    public void setValue(Number value) {
        this.value = value;
    }


    public String getTags(){
        return this.name.getTags();
    }

    public void setTags(String tags){
        this.name.setTags(tags);
    }

    public String getCounterType() {
        return counterType;
    }

    public void setCounterType(String counterType) {
        this.counterType = counterType;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }
}
