package com.tchepannou.kiosk.bot.service;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

public class MetricsService {
    private final String prefix;
    private final MetricRegistry metricRegistry;

    public MetricsService (String prefix, MetricRegistry metricRegistry){
        this.prefix = prefix;
        this.metricRegistry = metricRegistry;
    }

    public void markMeter (final String...name) {
        metricRegistry.meter(MetricRegistry.name(prefix, name)).mark();
    }

    public void markMeter (final long value, final String...name) {
        metricRegistry.meter(MetricRegistry.name(prefix, name)).mark(value);
    }

    public Timer.Context beginTimer (final String...name){
        return metricRegistry.timer(MetricRegistry.name(prefix, name)).time();
    }

    public long stopTimer(final Timer.Context tc){
        return tc.stop();
    }
}
