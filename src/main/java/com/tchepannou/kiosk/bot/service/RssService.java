package com.tchepannou.kiosk.bot.service;

import com.tchepannou.kiosk.bot.domain.RssItem;
import com.tchepannou.kiosk.client.dto.FeedDto;
import com.tchepannou.kiosk.client.dto.WebsiteDto;
import com.tchepannou.kiosk.core.service.LogService;
import com.tchepannou.kiosk.core.service.TimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public class RssService {
    @Autowired
    FeedService feedService;

    @Autowired
    TimeService timeService;

    @Autowired
    PublisherService publisherService;

    @Autowired
    WebsiteService websiteService;

    @Autowired
    RssGenerator rssGenerator;

    @Autowired
    FetcherService fetcherService;

    //-- Public
    @Scheduled(cron = "${kiosk.rss.cron}")
    public void run() {
        generate();
        fetch(true);
    }

    public void fetch(final boolean force) throws RssException {
        final List<FeedDto> feeds = feedService.getAllRssFeeds();
        for (final FeedDto feed : feeds) {
            fetch(feed, force);
        }
    }

    public void fetch(final long feedId, final boolean force) throws RssException {
        final FeedDto feed = feedService.findById(feedId);
        if (feed != null){
            fetch(feed, force);
        }
    }

    public void generate() {
        final Map<WebsiteDto, FeedDto> feeds = loadFeedsByWebsite();
        final List<WebsiteDto> websites = websiteService.getAllWebsite();
        for (final WebsiteDto website : websites) {
            generate(website, feeds);
        }
    }

    //-- Private
    private void fetch (final FeedDto feed, final boolean force){
        try {
            final Future<List<RssItem>> items = fetcherService.fetch(feed);
            publisherService.publish(feed, items.get(), force);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void generate(final WebsiteDto website, final Map<WebsiteDto, FeedDto> feeds) {
        final FeedDto feed = feeds.get(website);
        if (feed == null || !feed.getUrl().startsWith("s3://")) {
            return;
        }

        try {

            rssGenerator.generate(website);

        } catch (final Exception ex) {

            log(website, ex);

        }
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

        log.add("Step", "Generate");
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
