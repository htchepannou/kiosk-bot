package com.tchepannou.kiosk.bot.service;

import com.tchepannou.kiosk.bot.domain.RssItem;
import com.tchepannou.kiosk.client.dto.WebsiteDto;
import com.tchepannou.kiosk.core.service.TimeService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class HtmlService {
    @Autowired
    TimeService timeService;

    public List<String> extractUrls(final String html, final WebsiteDto website) throws IOException {
        final Document doc = Jsoup.parse(html);
        doc.setBaseUri(website.getUrl());
        final Set<String> urls = doc.select("a")
                .stream()
                .map(e -> e.attr("abs:href"))
                .filter(href -> isHttp(href))
                .collect(Collectors.toSet());

        return new ArrayList<>(urls);
    }

    public RssItem toRssItem(final String url, final String html, final WebsiteDto website) {
        final RssItem item = new RssItem();
        final Document doc = Jsoup.parse(html);

        item.setTitle(extractTitle(doc, website));
        item.setDescription(extractDescription(doc, website));
        item.setLink(url.toString());
        item.setPublishedDate(timeService.now());
        item.setCreationDate(timeService.now());

        return item;
    }

    protected String extractTitle(final Document doc, final WebsiteDto website) {
        String title = select(doc, website.getTitleCssSelector());
        if (title == null) {
            title = selectMeta(doc, "meta[property=og:title]");
        }
        return title;
    }

    protected String extractDescription(final Document doc, final WebsiteDto website) {
        String description = select(doc, website.getSlugCssSelector());
        if (description == null) {
            description = selectMeta(doc, "meta[property=og:description]");
        }
        return description;
    }

    private String select(final Document doc, final String cssSelector) {
        if (cssSelector == null) {
            return null;
        }

        final Elements elts = doc.select(cssSelector);
        return elts.isEmpty() ? null : elts.get(0).text();
    }

    private String selectMeta(final Document doc, final String cssSelector) {
        if (cssSelector == null) {
            return null;
        }

        final Elements elts = doc.select(cssSelector);
        return elts.isEmpty() ? null : elts.attr("content");
    }

    private boolean isHttp(final String url) {
        return url != null && url.toLowerCase().startsWith("http");
    }
}
