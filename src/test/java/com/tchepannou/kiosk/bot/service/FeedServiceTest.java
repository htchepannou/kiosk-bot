package com.tchepannou.kiosk.bot.service;

import com.tchepannou.kiosk.client.dto.FeedDto;
import com.tchepannou.kiosk.client.dto.GetFeedListResponse;
import com.tchepannou.kiosk.client.dto.KioskClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static com.tchepannou.kiosk.bot.Fixture.createFeed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FeedServiceTest {
    @Mock
    KioskClient kiosk;

    @InjectMocks
    FeedService service;

    @Test
    public void shouldReturnRssFeedDtos (){
        // Given
        final FeedDto FeedDto1 = createFeedDto("rss");
        final FeedDto FeedDto2 = createFeedDto("html");
        final FeedDto FeedDto3 = createFeedDto("rss");
        final FeedDto FeedDto4 = createFeedDto("rss");
        final GetFeedListResponse response = new GetFeedListResponse();
        response.setFeeds(Arrays.asList(FeedDto1, FeedDto2, FeedDto3, FeedDto4));

        when(kiosk.getFeeds()).thenReturn(response);

        // When
        List<FeedDto> FeedDtos = service.getAllRssFeeds();

        // Then
        assertThat(FeedDtos).containsExactly(FeedDto1, FeedDto3, FeedDto4);
    }

    private FeedDto createFeedDto(String type){
        final FeedDto FeedDto = createFeed();
        FeedDto.setType(type);

        return FeedDto;
    }
}
