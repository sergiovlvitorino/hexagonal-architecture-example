package com.sergiovitorino.hexagonalarchitectureexample.infrastructure.config.test;

import com.sergiovitorino.hexagonalarchitectureexample.infrastructure.config.RateLimitFilter;
import com.sergiovitorino.hexagonalarchitectureexample.infrastructure.config.RateLimitProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

public class RateLimitFilterTest {

    private RateLimitFilter newFilter(int capacity) {
        return new RateLimitFilter(new RateLimitProperties(capacity, 1, false, 100_000));
    }

    @Test
    void withinLimit_requestPasses() throws Exception {
        var filter = newFilter(100);
        var request = new MockHttpServletRequest("GET", "/rest/user");
        request.setRemoteAddr("10.0.0.1");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void afterExhaustingCapacity_returns429WithRetryAfterAndJsonBody() throws Exception {
        // capacity=1: primeira passa, segunda recebe 429
        var filter = newFilter(1);
        var ip = "10.0.0.2";

        // consume the single token
        var req1 = new MockHttpServletRequest("GET", "/rest/user");
        req1.setRemoteAddr(ip);
        filter.doFilter(req1, new MockHttpServletResponse(), new MockFilterChain());

        // next request should be rate-limited
        var req2 = new MockHttpServletRequest("GET", "/rest/user");
        req2.setRemoteAddr(ip);
        var resp2 = new MockHttpServletResponse();

        filter.doFilter(req2, resp2, new MockFilterChain());

        assertThat(resp2.getStatus()).isEqualTo(429);
        assertThat(resp2.getHeader("Retry-After")).isEqualTo("60");
        assertThat(resp2.getContentType()).contains("application/json");
        assertThat(resp2.getContentAsString()).isEqualTo("{\"error\":\"Too Many Requests\"}");
    }

    @Test
    void actuatorPath_isNotRateLimited() throws Exception {
        // capacity=1: exhaust the bucket via a normal path, then prove actuator still passes
        var filter = newFilter(1);
        var ip = "10.0.0.3";

        // exhaust the single token on a normal path
        var normalReq = new MockHttpServletRequest("GET", "/rest/user");
        normalReq.setRemoteAddr(ip);
        filter.doFilter(normalReq, new MockHttpServletResponse(), new MockFilterChain());

        // actuator must bypass the bucket entirely
        var actuatorReq = new MockHttpServletRequest("GET", "/actuator/health");
        actuatorReq.setRemoteAddr(ip);
        var actuatorResp = new MockHttpServletResponse();
        filter.doFilter(actuatorReq, actuatorResp, new MockFilterChain());

        assertThat(actuatorResp.getStatus()).isEqualTo(200);
    }

    @Test
    void trustProxy_withXff_usesFirstIpForRateLimiting() throws Exception {
        // With trustProxy=true and capacity=1, two requests from same XFF IP get rate-limited
        var filter = new RateLimitFilter(new RateLimitProperties(1, 1, true, 100_000));
        var ip = "1.2.3.4";

        var req1 = new MockHttpServletRequest("GET", "/rest/user");
        req1.addHeader("X-Forwarded-For", ip + ", 5.6.7.8");
        req1.setRemoteAddr("10.0.0.9");
        filter.doFilter(req1, new MockHttpServletResponse(), new MockFilterChain());

        var req2 = new MockHttpServletRequest("GET", "/rest/user");
        req2.addHeader("X-Forwarded-For", ip + ", 5.6.7.8");
        req2.setRemoteAddr("10.0.0.9");
        var resp2 = new MockHttpServletResponse();
        filter.doFilter(req2, resp2, new MockFilterChain());

        assertThat(resp2.getStatus()).isEqualTo(429);
    }

    @Test
    void withoutTrustProxy_xffIgnored_usesRemoteAddr() throws Exception {
        // Two requests with same XFF but different RemoteAddr should be treated as different IPs
        var filter = new RateLimitFilter(new RateLimitProperties(1, 1, false, 100_000));

        var req1 = new MockHttpServletRequest("GET", "/rest/user");
        req1.addHeader("X-Forwarded-For", "1.2.3.4");
        req1.setRemoteAddr("10.0.0.10");
        filter.doFilter(req1, new MockHttpServletResponse(), new MockFilterChain());

        // Different remote addr => different bucket => still has capacity
        var req2 = new MockHttpServletRequest("GET", "/rest/user");
        req2.addHeader("X-Forwarded-For", "1.2.3.4");
        req2.setRemoteAddr("10.0.0.11");
        var resp2 = new MockHttpServletResponse();
        filter.doFilter(req2, resp2, new MockFilterChain());

        assertThat(resp2.getStatus()).isEqualTo(200);
    }
}
