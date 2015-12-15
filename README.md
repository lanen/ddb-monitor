# ddb-monitor



# 如何使用 falcon


~~~~~~~~~~~~~~~~~

    @Bean
    @ConfigurationProperties("ddb.metrics.export")
    public FalconMetricProperties falconMetricProperties(){
        return new FalconMetricProperties();
    }


    @Bean
    public FalconMetricWriter metricWriter (FalconMetricProperties properties, MetricRegistry registry){
        FalconMetricWriter writer = new FalconMetricWriter(properties, registry);
        return writer;

    }

~~~~~~~~~~~~~~~~~

~~~~~~~~~~~~~~~~~

ddb.metrics.export.falcon.url = http\://192.168.0.16:1688/v1/push
ddb.metrics.export.falcon.endpoint = endpoint
ddb.metrics.export.falcon.step = 60
ddb.metrics.export.falcon.reportTimeMs = 10000
ddb.metrics.export.falcon.tags = tags

~~~~~~~~~~~~~~~~~

使用yml

~~~~~~~~~~~~~~~~~
ddb:
  metrics:
    export:
      falcon:
        url: http\://localhost:1688/v1/push
        endpoint: endpoint
        step: 60
        reportTimeMs: 60000
        tags: tags

~~~~~~~~~~~~~~~~~
