package com.tchepannou.kiosk.bot.support.rss;

import com.tchepannou.kiosk.bot.domain.RssItem;
import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RssSaxHandler extends DefaultHandler {
    //-- Attributes
    private static final Logger LOGGER = LoggerFactory.getLogger(RssSaxHandler.class);
    public static final String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss Z";

    private final List<RssItem> items = new ArrayList<>();
    private RssItem item = null;
    private StringBuilder text = null;
    private final DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);

    //-- Public
    public List<RssItem> getItems() {
        return items;
    }

    //-- DefaultHandler overrides
    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) {
        if ("item".equalsIgnoreCase(qName)) {
            item = new RssItem();
        }

        text = new StringBuilder();
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) {
        if (text != null) {
            text.append(ch, start, length);
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) {
        if ("item".equalsIgnoreCase(qName)) {
            items.add(item);
            item = null;
        } else if (item == null) {
            return;
        }

        switch (qName.toLowerCase()) {
            case "title":
                item.setTitle(html2text(text.toString()));
                break;

            case "description":
                item.setDescription(html2text(text.toString()));
                break;

            case "language":
                item.setLanguage(text.toString());
                break;

            case "link":
                item.setLink(text.toString());
                break;

            case "category":
                item.addCategory(StringEscapeUtils.unescapeHtml(text.toString()));
                break;

            case "pubdate":
                try {
                    item.setPublishedDate(dateFormat.parse(text.toString()));
                } catch (final ParseException e) {
                    LOGGER.warn("Invalid publish date", e);
                }
                break;
        }
    }

    private String html2text(final String html){
        return Jsoup.parse(html).body().text();
    }
}
