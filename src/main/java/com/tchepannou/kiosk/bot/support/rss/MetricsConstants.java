package com.tchepannou.kiosk.bot.support.rss;

public interface MetricsConstants {
    String PREFIX = "Kiosk.Bot.";

    String PUBLISH_SUCCESS = "PublishSuccess";
    String PUBLISH_ERROR = "PublishError";
    String PUBLISH_LATENCY = "PublishLatency";

    String FETCH_SUCCESS = "FetchSuccess";
    String FETCH_ERROR = "FetchError";
    String FETCH_LATENCY = "FetchLatency";

    String GENERATE_URL_SUCCESS = "GenerateUrlSuccess";
    String GENERATE_URL_ERROR = "GenerateUrlError";
}
