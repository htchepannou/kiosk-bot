package com.tchepannou.kiosk.bot.controller;

import com.tchepannou.kiosk.bot.service.RssService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PathVariable;
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
    @ApiOperation("Fetch data from all Rss feeds")
    @RequestMapping(
            value = "/fetch",
            params = {"force"},
            method = RequestMethod.GET
    )
    public void fetch(
            @ApiParam(defaultValue = "false", allowableValues = "false,true") final String force
    ) {
        rssService.fetch(Boolean.parseBoolean(force));
    }

    @Async
    @ApiOperation("Fetch data from a given Rss feed")
    @RequestMapping(
            value = "/fetch/feeds/{feedId}",
            params = {"force"},
            method = RequestMethod.GET
    )
    public void fetchFeed(
            @PathVariable final String feedId,
            @ApiParam(defaultValue = "false", allowableValues = "false,true") final String force
    ) {
        rssService.fetch(Long.parseLong(feedId), Boolean.parseBoolean(force));
    }

    @Async
    @ApiOperation("Generate RSS feeds")
    @RequestMapping(value = "/generate", method = RequestMethod.GET)
    public void generate() {
        rssService.generate();
    }
}
