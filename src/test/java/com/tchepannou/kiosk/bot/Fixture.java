package com.tchepannou.kiosk.bot;

import com.tchepannou.kiosk.bot.domain.RssItem;
import com.tchepannou.kiosk.client.dto.FeedDto;
import com.tchepannou.kiosk.client.dto.WebsiteDto;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;

public class Fixture {
    private static long uid = System.currentTimeMillis();

    public static long nextUid(){
        return ++uid;
    }

    public static FeedDto createFeed (){
        final long id = nextUid();
        final FeedDto feed = new FeedDto();
        feed.setCountryCode("USA");
        feed.setId(id);
        feed.setName("test");
        feed.setType("rss");
        feed.setUrl("http://www.feed.com/" + id);
        return feed;
    }

    public static RssItem createRssItem (){
        final RssItem item = new RssItem();
        item.setCategories(Arrays.asList("sport", "football"));
        item.setCountry("USA");
        item.setCreationDate(new Date());
        item.setDescription("This is the description");
        item.setLanguage("FR");
        item.setLink("http://www.item.com/" + nextUid());
        item.setPublishedDate(new Date());
        item.setTitle("This is the title");

        return item;
    }

    public static final InputStream createInputStream (final String content){
        return new ByteArrayInputStream(content.getBytes());
    }


    public static WebsiteDto createWebsite (){
        final long id = ++uid;
        WebsiteDto w = new WebsiteDto();
        w.setArticleUrlPrefix("article/");
        w.setArticleUrlSuffix(".html");
        w.setId(id);
        w.setName("foo" + id);
        w.setSlugCssSelector(".slug");
        w.setTitleCssSelector(".title");
        w.setUrl("http://" + w.getName() + ".com");
        return w;
    }

}
