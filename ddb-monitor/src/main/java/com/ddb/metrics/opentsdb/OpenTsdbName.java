package com.ddb.metrics.opentsdb;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author evan
 * @Date 2015年12月15日T00:18
 */
public class OpenTsdbName {

    private String metric;

    private String tags ;

    protected OpenTsdbName() {
    }

    public OpenTsdbName(String metric) {
        this.metric = metric;
    }

    public String getMetric() {
        return this.metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }


    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}
