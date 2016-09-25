package com.tchepannou.kiosk.bot.controller;

import com.tchepannou.kiosk.bot.service.RssService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(basePath = "/kiosk/v1/bots", value = "RSS API")
@RequestMapping(value = "/kiosk/v1/rss", produces = MediaType.APPLICATION_JSON_VALUE)
public class RssController {
    @Autowired
    RssService rssService;

    @Async
    @ApiOperation("Fetch data from Rss feeds")
    @RequestMapping(value = "/fetch", method = RequestMethod.GET)
    public void fetch() {
        rssService.fetch();
    }

    @Async
    @ApiOperation("Generate RSS feeds")
    @RequestMapping(value = "/generate", method = RequestMethod.GET)
    public void generate() {
        rssService.generate();
    }
}
