package com.tchepannou.kiosk.bot.config;

import com.tchepannou.kiosk.bot.service.FeedService;
import com.tchepannou.kiosk.bot.service.HtmlService;
import com.tchepannou.kiosk.bot.service.PublisherService;
import com.tchepannou.kiosk.bot.service.RssGenerator;
import com.tchepannou.kiosk.bot.service.RssService;
import com.tchepannou.kiosk.core.service.HttpService;
import com.tchepannou.kiosk.bot.service.WebsiteService;
import com.tchepannou.kiosk.client.dto.KioskClient;
import com.tchepannou.kiosk.client.dto.impl.DefaultKioskClient;
import com.tchepannou.kiosk.core.service.FileService;
import com.tchepannou.kiosk.core.service.TimeService;
import com.tchepannou.kiosk.core.service.UrlServiceProvider;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.io.File;

/**
 * Declare your services here!
 */
@Configuration
public class AppConfig {
    @Value("${kiosk.api.url}")
    String apiUrl;

    @Value("${kiosk.repository.home}")
    String repositoryHome;

    @Bean
    RestTemplate restTemplate(){
        return new RestTemplate();
    }

    @Bean
    RssGenerator rssGenerator() {
        return new RssGenerator();
    }

    @Bean
    HtmlService htmlService(){
        return new HtmlService();
    }

    @Bean
    KioskClient kioskClient () {
        return new DefaultKioskClient(apiUrl, restTemplate());
    }

    @Bean
    UrlServiceProvider urlServiceProvider(){
        UrlServiceProvider provider = new UrlServiceProvider();
        provider.register("http://", new HttpService());
        provider.register("https://", new HttpService());
        provider.register("s3://", new FileService(new File(repositoryHome)));

        return provider;
    }

    @Bean
    FeedService feedService (){
        return new FeedService();
    }

    @Bean
    PublisherService publisherService () {
        return new PublisherService();
    }

    @Bean
    RssService botService () {
        return new RssService();
    }

    @Bean
    TimeService timeService(){
        return new TimeService();
    }

    @Bean
    WebsiteService websiteService (){
        return new WebsiteService();
    }

    @Bean
    public VelocityEngine velocityEngine() {
        final VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();

        return ve;
    }

}
