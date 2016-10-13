package com.tchepannou.kiosk.bot.service;

import com.tchepannou.kiosk.bot.domain.RssItem;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PublisherService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PublisherService.class);

    @Autowired
    KioskClient kiosk;

    @Autowired
    UrlServiceProvider urlServiceProvider;

    @Autowired
    TimeService timeService;

    public void publish(
            final FeedDto feed,
            final RssItem item,
            final boolean force
    ) {
        PublishResponse response = null;
        try {

            final PublishRequest request = createPublishRequest(feed, item);
            request.setForce(force);
            response = kiosk.publishArticle(request);
            log(feed, item, response, null);

        } catch (final Exception ex) {

            log(feed, item, response, ex);

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
}
