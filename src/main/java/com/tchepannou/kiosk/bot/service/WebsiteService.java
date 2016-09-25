package com.tchepannou.kiosk.bot.service;

import com.tchepannou.kiosk.client.dto.GetWebsiteListResponse;
import com.tchepannou.kiosk.client.dto.KioskClient;
import com.tchepannou.kiosk.client.dto.WebsiteDto;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class WebsiteService {
    @Autowired
    private KioskClient kiosk;

    public List<WebsiteDto> getAllWebsite() {
        final GetWebsiteListResponse response = kiosk.getWebsites();

        return response.getWebsites();
    }
}
