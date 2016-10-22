package com.tchepannou.kiosk.bot.service;

import com.tchepannou.kiosk.bot.domain.RssItem;
import com.tchepannou.kiosk.client.dto.FeedDto;
import com.tchepannou.kiosk.core.service.TimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
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
        rssGenerator.generate();
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

    //-- Private
    private void fetch (final FeedDto feed, final boolean force){
        try {
            final Future<List<RssItem>> items = fetcherService.fetch(feed);
            publisherService.publish(feed, items.get(), force);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
