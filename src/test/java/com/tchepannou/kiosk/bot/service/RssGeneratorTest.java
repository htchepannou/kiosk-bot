package com.tchepannou.kiosk.bot.service;

import com.google.common.io.Files;
import com.tchepannou.kiosk.bot.domain.RssItem;
import com.tchepannou.kiosk.client.dto.WebsiteDto;
import com.tchepannou.kiosk.core.service.FileService;
import com.tchepannou.kiosk.core.service.HttpService;
import com.tchepannou.kiosk.core.service.TimeService;
import com.tchepannou.kiosk.core.service.UrlService;
import com.tchepannou.kiosk.core.service.UrlServiceProvider;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.OutputStream;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import static com.tchepannou.kiosk.bot.Fixture.createRssItem;
import static com.tchepannou.kiosk.bot.Fixture.createWebsite;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RssGeneratorTest {
    @Mock
    UrlServiceProvider urlServiceProvider;

    @Mock
    HttpService httpService;

    @Mock
    HtmlService htmlService;

    @Mock
    VelocityEngine velocity;

    @Mock
    TimeService timeService;

    @Mock
    FileService fileService;

    @InjectMocks
    RssGenerator generator;

    Date now;

    @Before
    public void setUp() {
        now = new Date();
        when(timeService.now()).thenReturn(now);

        when(urlServiceProvider.get(any())).thenAnswer(getUrlProvider());
    }

    @Test
    public void shouldGenerateRss() throws Exception {
        // Given
        final WebsiteDto website = createWebsite();
        website.setArticleUrlPrefix(null);
        website.setArticleUrlSuffix(null);

        final String html = "foo";
        doAnswer(get(html)).when(httpService).get(any(), any());
        when(htmlService.extractUrls(html, website)).thenReturn(
                Arrays.asList(
                        website.getUrl() + "/articles/foo.html",
                        website.getUrl() + "/articles/bar.html"
                )
        );

        final RssItem item1 = createRssItem();
        final RssItem item2 = createRssItem();
        when(htmlService.toRssItem(any(), any(), any()))
                .thenReturn(item1)
                .thenReturn(item2)
        ;

        // When
        final String key = generator.generate(website);

        // Then
        assertThat(key).isEqualTo("s3://rss/" + website.getId() + ".xml");

        final ArgumentCaptor<String> template = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> encoding = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Context> context = ArgumentCaptor.forClass(Context.class);
        final ArgumentCaptor<Writer> writer = ArgumentCaptor.forClass(Writer.class);
        verify(velocity).mergeTemplate(template.capture(), encoding.capture(), context.capture(), writer.capture());

        assertThat(template.getValue()).isEqualToIgnoringCase("template/rss.vm");
        assertThat(encoding.getValue()).isEqualToIgnoringCase("utf-8");

        assertThat(context.getValue().get("dateFormat")).isNotNull();
        assertThat(context.getValue().get("escape")).isInstanceOf(StringEscapeUtils.class);
        assertThat((Collection) context.getValue().get("items")).containsAll(Arrays.asList(item1, item2));
        assertThat(context.getValue().get("now")).isEqualTo(now);
        assertThat(context.getValue().get("website")).isEqualTo(website);
    }

    @Test
    public void shouldNotGenerateSameArticleTwice() throws Exception {
        // Given
        final WebsiteDto website = createWebsite();
        website.setArticleUrlPrefix(null);
        website.setArticleUrlSuffix(null);

        final String html = "foo";
        doAnswer(get(html)).when(httpService).get(any(), any());
        when(htmlService.extractUrls(html, website)).thenReturn(
                Arrays.asList(
                        website.getUrl() + "/articles/foo.html",
                        website.getUrl() + "/articles/bar.html"
                )
        );

        final RssItem item1 = createRssItem();
        final RssItem item2 = createRssItem();
        item2.setTitle(item1.getTitle());
        when(htmlService.toRssItem(any(), any(), any()))
                .thenReturn(item1)
                .thenReturn(item2)
        ;

        // When
        final String key = generator.generate(website);

        // Then
        final ArgumentCaptor<String> template = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> encoding = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Context> context = ArgumentCaptor.forClass(Context.class);
        final ArgumentCaptor<Writer> writer = ArgumentCaptor.forClass(Writer.class);
        verify(velocity).mergeTemplate(template.capture(), encoding.capture(), context.capture(), writer.capture());

        assertThat((Collection) context.getValue().get("items")).containsAll(Arrays.asList(item1));
    }

    @Test
    @Ignore
    public void shouldGenerateCameroonTribune() throws Exception {
        final UrlServiceProvider provider = new UrlServiceProvider();
        provider.register("http://", new HttpService());
        provider.register("s3://", new FileService(Files.createTempDir()));
        generator.urlServiceProvider = provider;
        generator.htmlService = new HtmlService();
        generator.htmlService.timeService = new TimeService();

        final WebsiteDto website = new WebsiteDto();
        website.setArticleUrlPrefix("/articles");
        website.setUrl("http://www.cameroon-tribune.cm");
        website.setTitleCssSelector("#article-post header h1");
        generator.generate(website);
    }

    private Answer get(final String content) {
        return new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocationOnMock) throws Throwable {
                final OutputStream out = (OutputStream) invocationOnMock.getArguments()[1];
                out.write(content.getBytes());
                return null;
            }
        };
    }

    private Answer<UrlService> getUrlProvider() {
        return new Answer() {
            @Override
            public UrlService answer(final InvocationOnMock invocationOnMock) throws Throwable {
                final String url = (String) invocationOnMock.getArguments()[0];
                if (url.startsWith("s3://")) {
                    return fileService;
                } else {
                    return httpService;
                }
            }
        };
    }
}
