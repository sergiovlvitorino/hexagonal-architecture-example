package com.sergiovitorino.hexagonalarchitectureexample.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ratelimit")
public record RateLimitProperties(int capacity, int refillMinutes, boolean trustProxy) {

    public RateLimitProperties {
        if (capacity <= 0) capacity = 100;
        if (refillMinutes <= 0) refillMinutes = 1;
    }
}
