package com.tchepannou.kiosk.bot.service;

import com.tchepannou.kiosk.bot.Fixture;
import com.tchepannou.kiosk.bot.domain.RssItem;
import com.tchepannou.kiosk.client.dto.ArticleDataDto;
import com.tchepannou.kiosk.client.dto.FeedDto;
import com.tchepannou.kiosk.client.dto.KioskClient;
import com.tchepannou.kiosk.client.dto.PublishRequest;
import com.tchepannou.kiosk.client.dto.PublishResponse;
import com.tchepannou.kiosk.core.service.HttpService;
import com.tchepannou.kiosk.core.service.TimeService;
import com.tchepannou.kiosk.core.service.UrlServiceProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.OutputStream;

import static com.tchepannou.kiosk.bot.Fixture.createFeed;
import static com.tchepannou.kiosk.bot.Fixture.createRssItem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PublisherServiceTest {
    @Mock
    KioskClient kiosk;

    @Mock
    HttpService httpService;

    @Mock
    UrlServiceProvider urlServiceProvider;

    @Mock
    TimeService timeService;

    @InjectMocks
    PublisherService service;

    @Test
    public void shouldPublish() throws Exception {

        // GIVEN
        final String content = "hello world";
        doAnswer(fetch(content)).when(httpService).get(any(), any());

        final PublishResponse response = createPublishResponse();
        when(kiosk.publishArticle(any())).thenReturn(response);

        when (timeService.format(any())).thenReturn("date");

        when (urlServiceProvider.get(any())).thenReturn(httpService);

        // WHEN
        final FeedDto feed = createFeed();
        final RssItem item = createRssItem();
        service.publish(feed, item);

        // THEN
        final ArgumentCaptor<PublishRequest> request = ArgumentCaptor.forClass(PublishRequest.class);
        verify(kiosk).publishArticle(request.capture());

        ArticleDataDto article = request.getValue().getArticle();
        assertThat(request.getValue().getFeedId()).isEqualTo(feed.getId());
        assertThat(article.getContent()).isEqualTo(content);
        assertThat(article.getCountryCode()).isEqualTo(item.getCountry());
        assertThat(article.getLanguageCode()).isEqualTo(item.getLanguage());
        assertThat(article.getPublishedDate()).isEqualTo("date");
        assertThat(article.getSlug()).isEqualTo(item.getDescription());
        assertThat(article.getTitle()).isEqualTo(item.getTitle());
        assertThat(article.getUrl()).isEqualTo(item.getLink());

    }

    private Answer fetch(final String content){
        return new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocationOnMock) throws Throwable {
                final OutputStream out = (OutputStream)invocationOnMock.getArguments()[1];
                out.write(content.getBytes());
                return null;
            }
        };
    }

    private PublishResponse createPublishResponse() {
        final PublishResponse response = new PublishResponse();
        response.setTransactionId(String.valueOf(Fixture.nextUid()));
        return response;
    }
}
