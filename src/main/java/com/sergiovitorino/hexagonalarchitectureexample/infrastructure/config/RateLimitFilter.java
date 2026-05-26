package com.sergiovitorino.hexagonalarchitectureexample.infrastructure.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

public class RateLimitFilter extends OncePerRequestFilter {

    private static final String ACTUATOR_PREFIX = "/actuator";

    private final Cache<String, Bucket> buckets;
    private final int capacity;
    private final int refillMinutes;
    private final boolean trustProxy;

    public RateLimitFilter(RateLimitProperties properties) {
        this.capacity = properties.capacity();
        this.refillMinutes = properties.refillMinutes();
        this.trustProxy = properties.trustProxy();
        this.buckets = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofMinutes(10))
                .maximumSize(properties.cacheMaximumSize())
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (request.getRequestURI().startsWith(ACTUATOR_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        var ip = resolveClientIp(request);
        var bucket = buckets.get(ip, this::newBucket);

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setHeader("Retry-After", "60");
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too Many Requests\"}");
        }
    }

    String resolveClientIp(HttpServletRequest request) {
        if (trustProxy) {
            var xff = request.getHeader("X-Forwarded-For");
            if (xff != null && !xff.isBlank()) {
                return xff.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    private Bucket newBucket(String ip) {
        var bandwidth = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(capacity, Duration.ofMinutes(refillMinutes))
                .build();
        return Bucket.builder().addLimit(bandwidth).build();
    }
}
