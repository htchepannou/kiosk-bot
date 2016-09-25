package com.tchepannou.kiosk.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class Starter {
    //-- Main
    public static void main(final String[] args) {
        SpringApplication.run(Starter.class, args);
    }
}
