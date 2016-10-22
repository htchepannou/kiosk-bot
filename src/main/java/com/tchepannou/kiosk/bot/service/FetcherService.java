package com.tchepannou.kiosk.bot.service;

import com.codahale.metrics.Timer;
import com.tchepannou.kiosk.bot.domain.RssItem;
import com.tchepannou.kiosk.bot.support.rss.MetricsConstants;
import com.tchepannou.kiosk.bot.support.rss.RssSaxHandler;
import com.tchepannou.kiosk.client.dto.FeedDto;
import com.tchepannou.kiosk.core.service.LogService;
import com.tchepannou.kiosk.core.service.TimeService;
import com.tchepannou.kiosk.core.service.UrlServiceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

public class FetcherService {
    @Autowired
    UrlServiceProvider urlServiceProvider;

    @Autowired
    FeedService feedService;

    @Autowired
    TimeService timeService;

    @Autowired
    MetricsService metricsService;

    @Async
    public Future<List<RssItem>> fetch(final FeedDto feed) {
        final List<RssItem> items = doFetch(feed);
        return new AsyncResult<>(items);
    }

    private List<RssItem> doFetch(final FeedDto feed) {
        List<RssItem> items = Collections.emptyList();
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        Throwable ex = null;
        final Timer.Context tc = metricsService.beginTimer(MetricsConstants.FETCH_LATENCY);
        try {
            final SAXParserFactory factory = SAXParserFactory.newInstance();
            final SAXParser sax = factory.newSAXParser();

            final String url = feed.getUrl();
            urlServiceProvider.get(url).get(url, out);

            final RssSaxHandler handler = new RssSaxHandler();
            sax.parse(new ByteArrayInputStream(out.toByteArray()), handler);
            items = handler.getItems();
            log(feed, items, null);

        } catch (final Exception e) {
            ex = e;
        } finally {

            markMetrics(tc, feed, items, ex);
            log(feed, Collections.emptyList(), ex);
        }

        return items;
    }

    private void log(final FeedDto feed, final List<RssItem> items, final Throwable ex) {
        final LogService log = new LogService(timeService);

        log.add("Step", "Fetch");
        log.add("ArticleCount", items.size());
        log.add("FeedId", feed.getId());
        log.add("FeedName", feed.getName());
        log.add("FeedURL", feed.getUrl());

        if (ex != null) {
            log.add("Exception", ex.getClass().getName());
            log.add("ExceptionMessage", ex.getMessage());

            log.log(ex);
        } else {
            log.log();
        }
    }

    private void markMetrics(final Timer.Context tc, final FeedDto feed, final List<RssItem> items, final Throwable ex) {
        metricsService.stopTimer(tc);

        if (ex == null) {
            markMeter(items.size(), MetricsConstants.FETCH_SUCCESS, feed);
        } else {
            markMeter(1, MetricsConstants.FETCH_ERROR, feed);
        }
    }

    private void markMeter(final int value, final String name, final FeedDto feed) {
        metricsService.markMeter(value, name);
        metricsService.markMeter(value, name, String.valueOf(feed.getId()));
    }

}
