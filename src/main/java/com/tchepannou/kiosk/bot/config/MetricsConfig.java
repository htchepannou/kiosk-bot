package com.tchepannou.kiosk.bot.config;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.tchepannou.kiosk.bot.service.MetricsService;
import com.tchepannou.kiosk.bot.support.rss.MetricsConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class MetricsConfig {
    @Bean
    MetricsService metricsService(){
        return new MetricsService(MetricsConstants.PREFIX, metricRegistry());
    }

    @Bean
    MetricRegistry metricRegistry() {
        final MetricRegistry metrics = new MetricRegistry();

        final JmxReporter jmx = JmxReporter.forRegistry(metrics)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        jmx.start();

        return metrics;
    }

}
