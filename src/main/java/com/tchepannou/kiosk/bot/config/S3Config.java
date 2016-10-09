package com.tchepannou.kiosk.bot.config;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("prod")
public class S3Config {
    @Bean
    AmazonS3 amazonS3() {
        return new AmazonS3Client();
    }
}
