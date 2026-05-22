package com.sergiovitorino.hexagonalarchitectureexample.infrastructure.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class SecurityHeadersFilter extends OncePerRequestFilter {

    private static final List<String> FULL_BYPASS_PREFIXES = List.of(
            "/graphiql",
            "/swagger-ui"
    );

    private static final String API_DOCS_PREFIX = "/v3/api-docs";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        var uri = request.getRequestURI();
        if (isFullBypassPath(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setHeader("X-Content-Type-Options", "nosniff");

        if (!uri.startsWith(API_DOCS_PREFIX)) {
            response.setHeader("X-Frame-Options", "DENY");
            response.setHeader("Referrer-Policy", "no-referrer");
            response.setHeader("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'");
        }

        filterChain.doFilter(request, response);
    }

    private boolean isFullBypassPath(String uri) {
        return FULL_BYPASS_PREFIXES.stream().anyMatch(uri::startsWith);
    }
}
