package com.ddb.metrics.falcon;


import com.codahale.metrics.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

/**
 * @author evan
 * @Date 2015年11月16日T14:23
 */
public class FalconReporter extends ScheduledReporter {

    public static final String FALCON_COUNTER = "COUNTER";
    public static final String FALCON_GAUGE = "GAUGE";
    /**
     * Returns a new {@link Builder} for {@link FalconReporter}.
     *
     * @param registry the registry to report
     * @return a {@link Builder} instance for a {@link FalconReporter}
     */
    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder(registry);
    }


    /**
     * A builder for {@link FalconReporter} instances. Defaults to not using a prefix, using the
     * default clock, converting rates to events/second, converting durations to milliseconds, and
     * not filtering metrics.
     */
    public static class Builder {
        private final MetricRegistry registry;
        private Clock clock;
        private String prefix;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private MetricFilter filter;

        private Builder(MetricRegistry registry) {
            this.registry = registry;
            this.clock = Clock.defaultClock();
            this.prefix = null;
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
        }

        /**
         * Use the given {@link Clock} instance for the time.
         *
         * @param clock a {@link Clock} instance
         * @return {@code this}
         */
        public Builder withClock(Clock clock) {
            this.clock = clock;
            return this;
        }

        /**
         * Prefix all metric names with the given string.
         *
         * @param prefix the prefix for all metric names
         * @return {@code this}
         */
        public Builder prefixedWith(String prefix) {
            this.prefix = prefix;
            return this;
        }

        /**
         * Convert rates to the given time unit.
         *
         * @param rateUnit a unit of time
         * @return {@code this}
         */
        public Builder convertRatesTo(TimeUnit rateUnit) {
            this.rateUnit = rateUnit;
            return this;
        }

        /**
         * Convert durations to the given time unit.
         *
         * @param durationUnit a unit of time
         * @return {@code this}
         */
        public Builder convertDurationsTo(TimeUnit durationUnit) {
            this.durationUnit = durationUnit;
            return this;
        }

        /**
         * Only report metrics which match the given filter.
         *
         * @param filter a {@link MetricFilter}
         * @return {@code this}
         */
        public Builder filter(MetricFilter filter) {
            this.filter = filter;
            return this;
        }

        /**
         * Builds a {@link FalconReporter} with the given properties, sending metrics using the
         * given {@link FalconSender}.
         *
         * @param falcon a {@link FalconSender}
         * @return a {@link FalconReporter}
         */
        public FalconReporter build(FalconSender falcon) {
            return new FalconReporter(registry,
                    falcon,
                    clock,
                    prefix,
                    rateUnit,
                    durationUnit,
                    filter);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(FalconReporter.class);

    private final FalconSender falcon;
    private final Clock clock;
    private final String prefix;;


    private FalconReporter(MetricRegistry registry,
                           FalconSender falcon,
                           Clock clock,
                           String prefix,
                           TimeUnit rateUnit,
                           TimeUnit durationUnit,
                           MetricFilter filter) {
        super(registry, "falcon-reporter", filter, rateUnit, durationUnit);
        this.falcon = falcon;
        this.clock = clock;
        this.prefix = prefix;

    }

    @Override
    public void report(SortedMap<String, Gauge> gauges,
                       SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms,
                       SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {
        final long timestamp = clock.getTime() / 1000;

        // oh it'd be lovely to use Java 7 here
        try {
            if (!falcon.isConnected()) {
                falcon.connect();
            }

            for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
                reportGauge(entry.getKey(), entry.getValue(), timestamp);
            }

            for (Map.Entry<String, Counter> entry : counters.entrySet()) {
                reportCounter(entry.getKey(), entry.getValue(), timestamp);
            }

            for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
                reportHistogram(entry.getKey(), entry.getValue(), timestamp);
            }

            for (Map.Entry<String, Meter> entry : meters.entrySet()) {
                reportMetered(entry.getKey(), entry.getValue(), timestamp);
            }

            for (Map.Entry<String, Timer> entry : timers.entrySet()) {
                reportTimer(entry.getKey(), entry.getValue(), timestamp);
            }

            falcon.flush();
            falcon.close();
        } catch (IOException e) {
            LOGGER.warn("Unable to report to falcon", falcon, e);
            try {
                falcon.close();
            } catch (IOException e1) {
                LOGGER.warn("Error closing falcon", falcon, e);
            }
        }
    }

    @Override
    public void stop() {
        try {
            super.stop();
        } finally {
            try {
                falcon.close();
            } catch (IOException e) {
                LOGGER.debug("Error disconnecting from falcon", falcon, e);
            }
        }
    }

    private void reportTimer(String name, Timer timer, long timestamp) throws IOException {
        final Snapshot snapshot = timer.getSnapshot();

        falcon.send(prefix(name, "max"), format(convertDuration(snapshot.getMax())), timestamp);
        falcon.send(prefix(name, "mean"), format(convertDuration(snapshot.getMean())), timestamp);
        falcon.send(prefix(name, "min"), format(convertDuration(snapshot.getMin())), timestamp);
        falcon.send(prefix(name, "stddev"),
                format(convertDuration(snapshot.getStdDev())),
                timestamp);
        falcon.send(prefix(name, "p50"),
                format(convertDuration(snapshot.getMedian())),
                timestamp);
        falcon.send(prefix(name, "p75"),
                format(convertDuration(snapshot.get75thPercentile())),
                timestamp);
        falcon.send(prefix(name, "p95"),
                format(convertDuration(snapshot.get95thPercentile())),
                timestamp);
        falcon.send(prefix(name, "p98"),
                format(convertDuration(snapshot.get98thPercentile())),
                timestamp);
        falcon.send(prefix(name, "p99"),
                format(convertDuration(snapshot.get99thPercentile())),
                timestamp);
        falcon.send(prefix(name, "p999"),
                format(convertDuration(snapshot.get999thPercentile())),
                timestamp);

        reportMetered(name, timer, timestamp);
    }

    private void reportMetered(String name, Metered meter, long timestamp) throws IOException {
        falcon.send(prefix(name, "count"), format(meter.getCount()), timestamp,FALCON_GAUGE);
        falcon.send(prefix(name, "m1_rate"),
                format(convertRate(meter.getOneMinuteRate())),
                timestamp,FALCON_GAUGE);
        falcon.send(prefix(name, "m5_rate"),
                format(convertRate(meter.getFiveMinuteRate())),
                timestamp,FALCON_GAUGE);
        falcon.send(prefix(name, "m15_rate"),
                format(convertRate(meter.getFifteenMinuteRate())),
                timestamp,FALCON_GAUGE);
        falcon.send(prefix(name, "mean_rate"),
                format(convertRate(meter.getMeanRate())),
                timestamp,FALCON_GAUGE);
    }


    private void reportHistogram(String name, Histogram histogram, long timestamp) throws IOException {
        final Snapshot snapshot = histogram.getSnapshot();
        falcon.send(prefix(name, "count"), format(histogram.getCount()), timestamp);
        falcon.send(prefix(name, "max"), format(snapshot.getMax()), timestamp);
        falcon.send(prefix(name, "mean"), format(snapshot.getMean()), timestamp);
        falcon.send(prefix(name, "min"), format(snapshot.getMin()), timestamp);
        falcon.send(prefix(name, "stddev"), format(snapshot.getStdDev()), timestamp);
        falcon.send(prefix(name, "p50"), format(snapshot.getMedian()), timestamp);
        falcon.send(prefix(name, "p75"), format(snapshot.get75thPercentile()), timestamp);
        falcon.send(prefix(name, "p95"), format(snapshot.get95thPercentile()), timestamp);
        falcon.send(prefix(name, "p98"), format(snapshot.get98thPercentile()), timestamp);
        falcon.send(prefix(name, "p99"), format(snapshot.get99thPercentile()), timestamp);
        falcon.send(prefix(name, "p999"), format(snapshot.get999thPercentile()), timestamp);
    }


    private void reportCounter(String name, Counter counter, long timestamp) throws IOException {
        falcon.send(prefix(name, "count"), format(counter.getCount()), timestamp, FALCON_COUNTER);
    }


    private void reportGauge(String name, Gauge gauge, long timestamp) throws IOException {
        final String value = format(gauge.getValue());
        if (value != null) {
            falcon.send(prefix(name), value, timestamp,FALCON_GAUGE);
        }
    }


    private String format(Object o) {
        if (o instanceof Float) {
            return format(((Float) o).doubleValue());
        } else if (o instanceof Double) {
            return format(((Double) o).doubleValue());
        } else if (o instanceof Byte) {
            return format(((Byte) o).longValue());
        } else if (o instanceof Short) {
            return format(((Short) o).longValue());
        } else if (o instanceof Integer) {
            return format(((Integer) o).longValue());
        } else if (o instanceof Long) {
            return format(((Long) o).longValue());
        }
        return null;
    }

    private String prefix(String... components) {
        return MetricRegistry.name(prefix, components);
    }

    private String format(long n) {
        return Long.toString(n);
    }

    private String format(double v) {
        // the Carbon plaintext format is pretty underspecified, but it seems like it just wants
        // US-formatted digits
        return String.format(Locale.US, "%2.2f", v);
    }
}
