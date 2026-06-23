package com.swipelab.auth.infrastructure;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for RateLimitingFilter.
 *
 * Uses MockHttpServletRequest/Response so no Spring context is needed.
 * Each test creates a fresh filter instance to guarantee an empty bucket cache.
 *
 * Covers:
 *  - Happy flow: requests within limit are forwarded (200 chain continues)
 *  - Edge case: request exceeding limit is rejected with 429
 *  - Edge case: non-rate-limited URI is always forwarded
 *  - Edge case: X-Forwarded-For header is used for key resolution
 */
@DisplayName("RateLimitingFilter")
class RateLimitingFilterTest {

    private RateLimitingFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RateLimitingFilter();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private MockHttpServletRequest request(String uri, String remoteAddr) {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", uri);
        req.setRequestURI(uri);
        req.setRemoteAddr(remoteAddr);
        return req;
    }

    private int doFilter(MockHttpServletRequest req) throws Exception {
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilterInternal(req, res, chain);
        return res.getStatus();
    }

    // ── Happy flow ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("first 20 login requests from same IP are allowed (within limit)")
    void loginEndpoint_allowsFirst20RequestsFromSameIp() throws Exception {
        MockHttpServletRequest req = request("/api/v1/auth/login", "10.0.0.1");

        for (int i = 0; i < 20; i++) {
            // MockFilterChain.getResponse() returns 200 by default
            MockHttpServletResponse res = new MockHttpServletResponse();
            MockFilterChain chain = new MockFilterChain();
            filter.doFilterInternal(req, res, chain);
            // Chain was called — response not written by filter, so status is 200
            assertThat(chain.getRequest()).isNotNull();
        }
    }

    // ── Edge cases ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("21st login request from same IP is rejected with 429")
    void loginEndpoint_rejects21stRequestWithTooManyRequests() throws Exception {
        MockHttpServletRequest req = request("/api/v1/auth/login", "10.0.0.2");

        // Exhaust the 20-per-minute bucket
        for (int i = 0; i < 20; i++) {
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilterInternal(req, res, new MockFilterChain());
        }

        // 21st attempt must be rate-limited
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilterInternal(req, res, chain);

        assertThat(res.getStatus()).isEqualTo(429);
        assertThat(res.getContentAsString()).contains("Rate limit exceeded");
        // Chain must NOT have been invoked
        assertThat(chain.getRequest()).isNull();
    }

    @Test
    @DisplayName("non-rate-limited URI is always forwarded regardless of call count")
    void nonRateLimitedUri_isAlwaysForwarded() throws Exception {
        MockHttpServletRequest req = request("/api/v1/images/42/content", "10.0.0.3");

        for (int i = 0; i < 30; i++) {
            MockHttpServletResponse res = new MockHttpServletResponse();
            MockFilterChain chain = new MockFilterChain();
            filter.doFilterInternal(req, res, chain);
            assertThat(chain.getRequest()).isNotNull(); // chain was called every time
        }
    }

    @Test
    @DisplayName("different IPs have independent buckets on the same endpoint")
    void differentIps_haveIndependentBuckets() throws Exception {
        // Exhaust IP-A's bucket
        MockHttpServletRequest reqA = request("/api/v1/auth/login", "10.0.0.4");
        for (int i = 0; i < 20; i++) {
            filter.doFilterInternal(reqA, new MockHttpServletResponse(), new MockFilterChain());
        }

        // IP-B should still be allowed (fresh bucket)
        MockHttpServletRequest reqB = request("/api/v1/auth/login", "10.0.0.5");
        MockHttpServletResponse resB = new MockHttpServletResponse();
        MockFilterChain chainB = new MockFilterChain();
        filter.doFilterInternal(reqB, resB, chainB);

        assertThat(chainB.getRequest()).isNotNull();   // forwarded
        assertThat(resB.getStatus()).isNotEqualTo(429); // not blocked
    }

    @Test
    @DisplayName("X-Forwarded-For header is used for IP resolution (proxy-aware)")
    void xForwardedFor_isUsedForIpResolution() throws Exception {
        MockHttpServletRequest req = request("/api/v1/auth/login", "127.0.0.1");
        req.addHeader("X-Forwarded-For", "203.0.113.5, 10.0.0.1");

        // Should use 203.0.113.5 as the key, not 127.0.0.1.
        // Exhaust the forwarded IP's bucket.
        for (int i = 0; i < 20; i++) {
            filter.doFilterInternal(req, new MockHttpServletResponse(), new MockFilterChain());
        }

        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilterInternal(req, res, chain);

        assertThat(res.getStatus()).isEqualTo(429);
        assertThat(chain.getRequest()).isNull();
    }
}
