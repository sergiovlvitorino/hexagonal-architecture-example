package com.sergiovitorino.hexagonalarchitectureexample.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ratelimit")
public record RateLimitProperties(int capacity, int refillMinutes, boolean trustProxy, int cacheMaximumSize) {

    public RateLimitProperties {
        if (capacity <= 0) capacity = 100;
        if (refillMinutes <= 0) refillMinutes = 1;
        if (cacheMaximumSize <= 0) cacheMaximumSize = 100_000;
    }
}
