package com.sergiovitorino.hexagonalarchitectureexample.infrastructure.config.test;

import com.sergiovitorino.hexagonalarchitectureexample.infrastructure.config.SecurityHeadersFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

public class SecurityHeadersFilterTest {

    private SecurityHeadersFilter filter;

    @BeforeEach
    void setUp() {
        filter = new SecurityHeadersFilter();
    }

    @Test
    void normalPath_addsAllFourSecurityHeaders() throws Exception {
        var request = new MockHttpServletRequest("GET", "/rest/user");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader("X-Content-Type-Options")).isEqualTo("nosniff");
        assertThat(response.getHeader("X-Frame-Options")).isEqualTo("DENY");
        assertThat(response.getHeader("Referrer-Policy")).isEqualTo("no-referrer");
        assertThat(response.getHeader("Content-Security-Policy")).isEqualTo("default-src 'none'; frame-ancestors 'none'");
    }

    @Test
    void swaggerUiPath_doesNotAddAnySecurityHeaders() throws Exception {
        var request = new MockHttpServletRequest("GET", "/swagger-ui/index.html");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader("X-Content-Type-Options")).isNull();
        assertThat(response.getHeader("X-Frame-Options")).isNull();
        assertThat(response.getHeader("Referrer-Policy")).isNull();
        assertThat(response.getHeader("Content-Security-Policy")).isNull();
    }

    @Test
    void graphiqlPath_doesNotAddAnySecurityHeaders() throws Exception {
        var request = new MockHttpServletRequest("GET", "/graphiql");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader("X-Content-Type-Options")).isNull();
        assertThat(response.getHeader("X-Frame-Options")).isNull();
        assertThat(response.getHeader("Referrer-Policy")).isNull();
        assertThat(response.getHeader("Content-Security-Policy")).isNull();
    }

    @Test
    void apiDocsPath_addsOnlyNosniffAndSkipsOtherHeaders() throws Exception {
        var request = new MockHttpServletRequest("GET", "/v3/api-docs/swagger-config");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader("X-Content-Type-Options")).isEqualTo("nosniff");
        assertThat(response.getHeader("X-Frame-Options")).isNull();
        assertThat(response.getHeader("Referrer-Policy")).isNull();
        assertThat(response.getHeader("Content-Security-Policy")).isNull();
    }
}
