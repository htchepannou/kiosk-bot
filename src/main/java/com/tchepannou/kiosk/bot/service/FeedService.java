package com.tchepannou.kiosk.bot.service;

import com.tchepannou.kiosk.client.dto.FeedDto;
import com.tchepannou.kiosk.client.dto.GetFeedListResponse;
import com.tchepannou.kiosk.client.dto.KioskClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

public class FeedService {
    @Autowired
    private KioskClient kiosk;

    public List<FeedDto> getAllRssFeeds() {
        final GetFeedListResponse response = kiosk.getFeeds();

        return response.getFeeds()
                .stream()
                .filter(dto -> "rss".equalsIgnoreCase(dto.getType()))
                .collect(Collectors.toList());
    }
}
