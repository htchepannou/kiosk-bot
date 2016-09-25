package com.tchepannou.kiosk.bot.service;

import com.tchepannou.kiosk.bot.domain.RssItem;
import com.tchepannou.kiosk.bot.support.rss.RssSaxHandler;
import com.tchepannou.kiosk.client.dto.WebsiteDto;
import com.tchepannou.kiosk.core.service.LogService;
import com.tchepannou.kiosk.core.service.TimeService;
import com.tchepannou.kiosk.core.service.UrlServiceProvider;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RssGenerator {
    @Autowired
    HtmlService htmlService;

    @Autowired
    VelocityEngine velocity;

    @Autowired
    TimeService timeService;

    @Autowired
    UrlServiceProvider urlServiceProvider;

    final String generate(final WebsiteDto website) throws IOException {
        try (final ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            final String websiteUrl = website.getUrl();
            urlServiceProvider.get(websiteUrl).get(websiteUrl, out);

            // Generate the RSS
            final String html = out.toString();
            final List<String> urls = htmlService.extractUrls(html, website);
            final Map<String, RssItem> items = new LinkedHashMap<>();
            for (final String url : urls) {
                if (!isArticle(url, website)) {
                    continue;
                }

                try {
                    final RssItem item = toRssItem(url, website);
                    final String title = item.getTitle();
                    if (isValid(item) && !items.containsKey(title)) {
                        // prevent same article under different URLs
                        items.put(title, item);
                        log(url, website, item, null);
                    }
                } catch (final Exception e) {
                    log(url, website, null, e);
                }
            }

            final String rss = toRss(website, items.values());

            // Store it
            final String key = "s3://rss/" + website.getId() + ".xml";
            urlServiceProvider.get(key).put(key, new ByteArrayInputStream(rss.getBytes()));

            return key;
        }
    }

    private void log(final String url, final WebsiteDto website, final RssItem item, final Throwable ex) {
        final LogService log = new LogService(timeService);

        log.add("WebsiteId", website.getId());
        log.add("WebsiteName", website.getName());
        log.add("Url", url);

        if (item != null) {
            log.add("Title", item.getTitle());
        }

        if (ex == null) {

            log.add("Success", true);
            log.log();

        } else {

            log.add("Success", false);
            log.add("Exception", ex.getClass().getName());
            log.add("ExceptionMessage", ex.getMessage());
            log.log(ex);

        }
    }

    private RssItem toRssItem(final String url, final WebsiteDto website) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        urlServiceProvider.get(url).get(url, out);

        final String html = out.toString();
        return htmlService.toRssItem(url, html, website);
    }

    private String toRss(final WebsiteDto website, final Collection<RssItem> items) {
        final StringWriter writer = new StringWriter();

        final VelocityContext context = new VelocityContext();
        context.put("dateFormat", new SimpleDateFormat(RssSaxHandler.DATE_FORMAT));
        context.put("escape", new StringEscapeUtils());
        context.put("items", items);
        context.put("now", timeService.now());
        context.put("website", website);

        velocity.mergeTemplate("template/rss.vm", "utf-8", context, writer);

        return writer.toString();
    }

    private boolean isValid(final RssItem item) {
        return item.getTitle() != null && item.getLink() != null;
    }

    private boolean isArticle(final String url, final WebsiteDto website) throws IOException {
        final String prefix = website.getArticleUrlPrefix();
        final String suffix = website.getArticleUrlSuffix();
        final String path = new URL(url).getPath();

        return url.startsWith(website.getUrl())
                && (prefix == null || (path.length()>prefix.length() && path.startsWith(prefix)))
                && (suffix == null || path.endsWith(suffix))
                ;
    }
}
