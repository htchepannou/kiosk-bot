package com.tchepannou.kiosk.bot.service;

import com.codahale.metrics.Timer;
import com.tchepannou.kiosk.bot.domain.RssItem;
import com.tchepannou.kiosk.bot.support.rss.MetricsConstants;
import com.tchepannou.kiosk.client.dto.ArticleDataDto;
import com.tchepannou.kiosk.client.dto.ErrorDto;
import com.tchepannou.kiosk.client.dto.FeedDto;
import com.tchepannou.kiosk.client.dto.KioskClient;
import com.tchepannou.kiosk.client.dto.KioskClientException;
import com.tchepannou.kiosk.client.dto.PublishRequest;
import com.tchepannou.kiosk.client.dto.PublishResponse;
import com.tchepannou.kiosk.core.service.LogService;
import com.tchepannou.kiosk.core.service.TimeService;
import com.tchepannou.kiosk.core.service.UrlServiceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class PublisherService {
    @Autowired
    KioskClient kiosk;

    @Autowired
    UrlServiceProvider urlServiceProvider;

    @Autowired
    TimeService timeService;

    @Autowired
    MetricsService metricsService;

    @Async
    public void publish(final FeedDto feed, final List<RssItem> items, final boolean force) {
        for (final RssItem item : items) {
            publish(feed, item, force);
        }
    }

    protected void publish(
            final FeedDto feed,
            final RssItem item,
            final boolean force
    ) {
        Throwable ex = null;
        PublishResponse response = null;

        final Timer.Context tc = metricsService.beginTimer(MetricsConstants.PUBLISH_LATENCY);
        try {

            final PublishRequest request = createPublishRequest(feed, item);
            request.setForce(force);
            response = kiosk.publishArticle(request);

        } catch (final Exception e) {

            ex = e;

        } finally {
            log(feed, item, response, ex);
            markMetrics(tc, feed, ex);
        }
    }

    private PublishRequest createPublishRequest(
            final FeedDto feed,
            final RssItem item
    ) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final String url = item.getLink();
        urlServiceProvider.get(url).get(url, out);

        final ArticleDataDto article = new ArticleDataDto();
        article.setContent(out.toString("utf-8"));
        article.setCountryCode(item.getCountry());
        article.setLanguageCode(item.getLanguage());
        article.setPublishedDate(timeService.format(item.getPublishedDate()));
        article.setSlug(item.getDescription());
        article.setTitle(item.getTitle());
        article.setUrl(item.getLink());

        final PublishRequest request = new PublishRequest();
        request.setFeedId(feed.getId());
        request.setArticle(article);

        return request;
    }

    private void log(
            final FeedDto feed,
            final RssItem item,
            final PublishResponse response,
            final Throwable ex
    ) {
        final LogService logger = new LogService(timeService);

        logger.add("Step", "Publish");
        logger.add("ArticleURL", item.getLink());
        logger.add("ArticleTitle", item.getTitle());
        logger.add("FeedId", feed.getId());

        if (ex == null) {

            logger.add("Success", response.isSuccess());
            logger.log();

        } else {

            logger.add("Success", false);
            logger.add("Exception", ex.getClass().getName());
            logger.add("ExceptionMessage", ex.getMessage());
            if (ex instanceof KioskClientException) {
                final ErrorDto error = ((KioskClientException) ex).getError();
                if (error != null) {
                    logger.add("ErrorCode", error.getCode());
                    logger.add("ErrorMessage", error.getMessage());
                }
                logger.log();
            } else {
                logger.log(ex);
            }
        }
    }

    private void markMetrics(final Timer.Context tc, final FeedDto feed, final Throwable ex) {
        metricsService.stopTimer(tc);

        if (ex == null) {
            markMeter(MetricsConstants.PUBLISH_SUCCESS, feed);
        } else {
            markMeter(MetricsConstants.PUBLISH_ERROR, feed);
        }
    }

    private void markMeter(final String name, final FeedDto feed) {
        metricsService.markMeter(name);
        metricsService.markMeter(name, String.valueOf(feed.getId()));
    }
}
