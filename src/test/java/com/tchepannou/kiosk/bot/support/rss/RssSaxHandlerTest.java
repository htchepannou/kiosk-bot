package com.tchepannou.kiosk.bot.support.rss;

import com.tchepannou.kiosk.bot.domain.RssItem;
import org.junit.Test;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;



public class RssSaxHandlerTest {

    @Test
    public void load () throws Exception{
        final SAXParserFactory factory = SAXParserFactory.newInstance();
        final SAXParser saxParser = factory.newSAXParser();
        final RssSaxHandler handler = new RssSaxHandler();
        final DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

        InputStream in = getClass().getResourceAsStream("/rss.xml");
        saxParser.parse(in, handler);

        List<RssItem> items = handler.getItems();

        assertThat(items).hasSize(2);

        RssItem item = items.get(0);
        assertThat(item.getPublishedDate()).isEqualTo(df.parse("Sun, 06 Sep 2009 16:20:00 +0000"));
        assertThat(item.getCategories()).containsExactly("sport", "football");
        assertThat(item.getCountry()).isNull();
        assertThat(item.getDescription()).isEqualTo("Here is some text containing an interesting description.");
        assertThat(item.getLanguage()).isEqualTo("fr_CA");
        assertThat(item.getLink()).isEqualTo("http://www.example.com/blog/post/1");
        assertThat(item.getTitle()).isEqualTo("Example entry");

        item = items.get(1);
        assertThat(item.getPublishedDate()).isEqualTo(df.parse("Sun, 07 Sep 2009 16:20:00 +0000"));
        assertThat(item.getCountry()).isNull();
        assertThat(item.getDescription()).isEqualTo("Here is some text containing an interesting description #2");
        assertThat(item.getLanguage()).isEqualTo("en_US");
        assertThat(item.getLink()).isEqualTo("http://www.example.com/blog/post/2");
        assertThat(item.getCategories()).isEmpty();
        assertThat(item.getTitle()).isEqualTo("Example entry #2");

    }
}
