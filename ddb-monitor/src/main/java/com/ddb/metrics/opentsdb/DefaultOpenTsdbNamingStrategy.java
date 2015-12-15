package com.ddb.metrics.opentsdb;


import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A naming strategy that just passes through the metric name, together with tags from a
 * set of static values. Open TSDB requires at least one tag, so tags are always added for
 * you: the {@value #DOMAIN_KEY} key is added with a value "spring", and the
 * {@value #PROCESS_KEY} key is added with a value equal to the object hash of "this" (the
 * naming strategy). The "domain" value is a system identifier - it would be common to all
 * processes in the same distributed system. In most cases this will be unique enough to
 * allow aggregation of the underlying metrics in Open TSDB, but normally it is best to
 * provide your own tags, including a prefix and process identifier if you know one
 * (overwriting the default).
 *
 * @author Dave Syer
 * @since 1.3.0
 */
public class DefaultOpenTsdbNamingStrategy implements OpenTsdbNamingStrategy {

    /**
     * The domain key.
     */
    public static final String DOMAIN_KEY = "domain";

    /**
     * The process key.
     */
    public static final String PROCESS_KEY = "process";

    /**
     * Tags to apply to every metric. Open TSDB requires at least one tag, so a "prefix"
     * tag is added for you by default.
     */
    private Map<String, String> tags = new LinkedHashMap<String, String>();

    private Map<String, OpenTsdbName> cache = new HashMap<String, OpenTsdbName>();

    public DefaultOpenTsdbNamingStrategy() {
        this.tags.put(DOMAIN_KEY, "org.springframework.metrics");
        this.tags.put(PROCESS_KEY, ObjectUtils.getIdentityHexString(this));
    }

    public void setTags(Map<String, String> staticTags) {
        this.tags.putAll(staticTags);
    }

    @Override
    public OpenTsdbName getName(String name) {
        if (this.cache.containsKey(name)) {
            return this.cache.get(name);
        }
        OpenTsdbName value = new OpenTsdbName(name);
        value.setTags(tags.get(name));

        this.cache.put(name, value);
        return value;
    }

}