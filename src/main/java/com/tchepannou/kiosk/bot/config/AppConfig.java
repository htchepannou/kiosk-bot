package com.tchepannou.kiosk.bot.config;

import com.amazonaws.services.s3.AmazonS3;
import com.tchepannou.kiosk.bot.service.FeedService;
import com.tchepannou.kiosk.bot.service.FetcherService;
import com.tchepannou.kiosk.bot.service.HtmlService;
import com.tchepannou.kiosk.bot.service.PublisherService;
import com.tchepannou.kiosk.bot.service.RssGenerator;
import com.tchepannou.kiosk.bot.service.RssService;
import com.tchepannou.kiosk.bot.service.WebsiteService;
import com.tchepannou.kiosk.client.dto.KioskClient;
import com.tchepannou.kiosk.client.dto.impl.DefaultKioskClient;
import com.tchepannou.kiosk.core.service.FileService;
import com.tchepannou.kiosk.core.service.HttpService;
import com.tchepannou.kiosk.core.service.S3Service;
import com.tchepannou.kiosk.core.service.TimeService;
import com.tchepannou.kiosk.core.service.UrlServiceProvider;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Declare your services here!
 */
@Configuration
public class AppConfig {
    @Value("${kiosk.api.url}")
    String apiUrl;

    @Value("${kiosk.executor.threads}")
    int executorThreads;

    @Bean
    ExecutorService executorService() {
        return Executors.newFixedThreadPool(executorThreads);
    }

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    RssGenerator rssGenerator() {
        return new RssGenerator();
    }

    @Bean
    HtmlService htmlService() {
        return new HtmlService();
    }

    @Bean
    KioskClient kioskClient() {
        return new DefaultKioskClient(apiUrl, restTemplate());
    }

    @Bean
    FetcherService fetcherService () {
        return new FetcherService();
    }

    @Bean
    @Autowired
    UrlServiceProvider urlServiceProvider(
            final HttpService httpService,
            final FileService fileService
    ) {
        final UrlServiceProvider provider = new UrlServiceProvider();
        provider.register("http://", httpService);
        provider.register("https://", httpService);
        provider.register("s3://", fileService);

        return provider;
    }

    @Bean
    HttpService httpService() {
        return new HttpService();
    }

    @Bean
    @Profile("!prod")
    FileService localFileService(
            @Value("${kiosk.repository.home}") final String repositoryHome
    ) {
        return new FileService(new File(repositoryHome));
    }

    @Bean
    @Profile("prod")
    FileService s3FileService(
            @Value("${kiosk.aws.s3.bucket}") final String bucket,
            final AmazonS3 s3
    ) {
        return new S3Service(bucket, s3);
    }

    @Bean
    FeedService feedService() {
        return new FeedService();
    }

    @Bean
    PublisherService publisherService() {
        return new PublisherService();
    }

    @Bean
    RssService botService() {
        return new RssService();
    }

    @Bean
    TimeService timeService() {
        return new TimeService();
    }

    @Bean
    WebsiteService websiteService() {
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
