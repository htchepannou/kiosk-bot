package com.tchepannou.kiosk.bot.controller;

import com.tchepannou.kiosk.bot.service.RssGenerator;
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
@Api(basePath = "/kiosk/v1/rss", value = "RSS API")
@RequestMapping(value = "/kiosk/v1/rss", produces = MediaType.APPLICATION_JSON_VALUE)
public class RssController {
    @Autowired
    RssService service;

    @Autowired
    RssGenerator generator;

    @Async
    @ApiOperation("Run the RSS bot")
    @RequestMapping(
            value = "/run",
            method = RequestMethod.GET
    )
    public void run() {
        service.run();
    }

    @Async
    @ApiOperation("Fetch data from a given Rss feed")
    @RequestMapping(
            value = "/fetch/feeds/{feedId}",
            params = {"force"},
            method = RequestMethod.GET
    )
    public void fetch(
            @PathVariable final String feedId,
            @ApiParam(defaultValue = "false", allowableValues = "false,true") final String force
    ) {
        service.fetch(Long.parseLong(feedId), Boolean.parseBoolean(force));
    }

    @Async
    @ApiOperation("Generate RSS feeds")
    @RequestMapping(
            value = "/generate",
            method = RequestMethod.GET
    )
    public void generate(){
        generator.generate();
    }


    @Async
    @ApiOperation("Generate RSS feeds of 1 website")
    @RequestMapping(
            value = "/generate/website/{websiteId}",
            method = RequestMethod.GET
    )
    public void generate(@PathVariable("websiteId") final String websiteId){
        generator.generate(websiteId);
    }
}
