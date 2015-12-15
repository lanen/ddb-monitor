package com.ddb.metrics.falcon;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 *
 * falcon 的配置
 * @author evan
 * @Date 2015年12月14日T21:52
 */
@ConfigurationProperties("ddb.metrics.export")
public class FalconMetricProperties {

    private Falcon falcon = new Falcon();

    public Falcon getFalcon() {
        return falcon;
    }

    public void setFalcon(Falcon falcon) {
        this.falcon = falcon;
    }

    /**
     *
     */
    public static class Falcon {

        private String url;

        private String endpoint;

        private int step;

        private int reportTimeMs;

        private String tags;

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

        public int getReportTimeMs() {
            return reportTimeMs;
        }

        public void setReportTimeMs(int reportTimeMs) {
            this.reportTimeMs = reportTimeMs;
        }

        public String getTags() {
            return tags;
        }

        public void setTags(String tags) {
            this.tags = tags;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
