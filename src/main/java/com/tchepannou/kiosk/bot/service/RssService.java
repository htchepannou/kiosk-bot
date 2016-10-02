package com.tchepannou.kiosk.bot.service;

import com.tchepannou.kiosk.bot.domain.RssItem;
import com.tchepannou.kiosk.bot.support.rss.RssSaxHandler;
import com.tchepannou.kiosk.client.dto.FeedDto;
import com.tchepannou.kiosk.client.dto.KioskClient;
import com.tchepannou.kiosk.client.dto.WebsiteDto;
import com.tchepannou.kiosk.core.service.LogService;
import com.tchepannou.kiosk.core.service.TimeService;
import com.tchepannou.kiosk.core.service.UrlServiceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class RssService {
    @Autowired
    UrlServiceProvider urlServiceProvider;

    @Autowired
    FeedService feedService;

    @Autowired
    KioskClient kiosk;

    @Autowired
    TimeService timeService;

    @Autowired
    PublisherService publisherService;

    @Autowired
    WebsiteService websiteService;

    @Autowired
    RssGenerator rssGenerator;

    @Autowired
    ExecutorService executorService;

    //-- Fetch
    public void fetch(final boolean force) throws RssException {
        final List<FeedDto> feeds = feedService.getAllRssFeeds();
        for (final FeedDto feed : feeds) {
            executorService.submit(createFectcher(feed, force));
        }
    }

    public void fetch(final long id, final boolean force) throws RssException {
        final FeedDto feed = feedService.findById(id);
        if (feed != null) {
            executorService.submit(createFectcher(feed, force));
        }
    }

    private Runnable createFectcher(final FeedDto feed, final boolean force) {
        return new Runnable() {
            @Override
            public void run() {
                try {

                    final SAXParserFactory factory = SAXParserFactory.newInstance();
                    final SAXParser sax = factory.newSAXParser();

                    final List<RssItem> items = fetch(feed, sax);
                    publish(feed, items, force);

                } catch (final Exception e) {
                    throw new RssException("XML error", e);
                }
            }
        };
    }

    private List<RssItem> fetch(final FeedDto feed, final SAXParser sax) throws SAXException {
        List<RssItem> items = Collections.emptyList();
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            final String url = feed.getUrl();
            urlServiceProvider.get(url).get(url, out);

            final RssSaxHandler handler = new RssSaxHandler();
            sax.parse(new ByteArrayInputStream(out.toByteArray()), handler);
            items = handler.getItems();
            log(feed, items, null);

        } catch (final IOException ex) {

            log(feed, Collections.emptyList(), ex);

        }

        return items;
    }

    private void publish(final FeedDto feed, final List<RssItem> items, final boolean force) {

        for (final RssItem item : items) {
            publisherService.publish(feed, item, force);
        }
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

    //-- Generate
    public void generate() {
        final Map<WebsiteDto, FeedDto> feeds = loadFeedsByWebsite();
        final List<WebsiteDto> websites = websiteService.getAllWebsite();
        for (final WebsiteDto website : websites) {
            executorService.execute(createGenerator(website, feeds));
        }
    }

    private Runnable createGenerator(final WebsiteDto website, final Map<WebsiteDto, FeedDto> feeds) {
        return new Runnable() {
            @Override
            public void run() {
                final FeedDto feed = feeds.get(website);
                if (feed == null || !feed.getUrl().startsWith("s3://")) {
                    return;
                }

                Throwable ex = null;
                try {

                    rssGenerator.generate(website);
                } catch (final Exception e) {
                    ex = e;
                } finally {
                    log(website, ex);
                }
            }
        };
    }

    private Map<WebsiteDto, FeedDto> loadFeedsByWebsite() {
        final List<FeedDto> feeds = feedService.getAllRssFeeds();
        final Map<WebsiteDto, FeedDto> feedMap = new HashMap<>();
        for (final FeedDto feed : feeds) {
            feedMap.put(feed.getWebsite(), feed);
        }
        return feedMap;
    }

    private void log(final WebsiteDto website, final Throwable ex) {
        final LogService log = new LogService(timeService);

        log.add("WebsiteId", website.getId());
        log.add("WebsiteName", website.getName());
        log.add("WebsiteUrl", website.getUrl());

        if (ex != null) {

            log.add("Exception", ex.getClass().getName());
            log.add("ExceptionMessage", ex.getMessage());
            log.log(ex);

        } else {
            log.log();
        }
    }

}
