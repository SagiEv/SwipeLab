package com.swipelab.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the ShallowEtagHeaderFilter behaviour directly using MockServlet objects.
 *
 * Verifies:
 *  - First GET → 200 with ETag header present
 *  - Second GET with matching If-None-Match → 304 Not Modified with empty body
 *  - Stale ETag → 200 with a new ETag value
 *  - Binary-excluded paths → no ETag header (shouldNotFilter override)
 */
class ETagIntegrationTest {

    /**
     * Inline subclass mirroring the shouldNotFilter logic from HttpCacheConfig.
     */
    private static class TestEtagFilter extends ShallowEtagHeaderFilter {
        @Override
        protected boolean shouldNotFilter(HttpServletRequest request) {
            String path = request.getRequestURI();
            return path.matches(".*/images/[^/]+/content")
                    || path.matches(".*/gold-images/[^/]+/image");
        }
    }

    // ── Servlet stubs that write a fixed body ──────────────────────────────────

    /** Writes a JSON leaderboard body. */
    private static final HttpServlet LEADERBOARD_SERVLET = new HttpServlet() {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse res)
                throws java.io.IOException {
            res.setContentType("application/json");
            res.getWriter().write("{\"data\":\"leaderboard\"}");
        }
    };

    /** Writes a different JSON body to produce a new ETag. */
    private static final HttpServlet UPDATED_LEADERBOARD_SERVLET = new HttpServlet() {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse res)
                throws java.io.IOException {
            res.setContentType("application/json");
            res.getWriter().write("{\"data\":\"updated-leaderboard\"}");
        }
    };

    /** Writes binary image bytes. */
    private static final HttpServlet IMAGE_SERVLET = new HttpServlet() {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse res)
                throws java.io.IOException {
            res.setContentType("image/jpeg");
            res.getOutputStream().write(new byte[]{(byte) 0xFF, (byte) 0xD8});
        }
    };

    private TestEtagFilter filter;

    @BeforeEach
    void setUp() {
        filter = new TestEtagFilter();
    }

    // ── Happy path: 200 + ETag on first request ────────────────────────────────

    @Test
    void firstRequest_ShouldReturn200_WithEtagHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/gamification/leaderboard");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain(LEADERBOARD_SERVLET));

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeader("ETag")).isNotNull();
    }

    // ── Happy path: matching ETag → 304 ───────────────────────────────────────

    @Test
    void secondRequest_WithMatchingEtag_ShouldReturn304_WithEmptyBody() throws Exception {
        // First request to capture ETag
        MockHttpServletRequest firstRequest = new MockHttpServletRequest("GET", "/api/v1/gamification/leaderboard");
        MockHttpServletResponse firstResponse = new MockHttpServletResponse();
        filter.doFilter(firstRequest, firstResponse, new MockFilterChain(LEADERBOARD_SERVLET));
        String etag = firstResponse.getHeader("ETag");
        assertThat(etag).isNotNull();

        // Second request with If-None-Match
        MockHttpServletRequest secondRequest = new MockHttpServletRequest("GET", "/api/v1/gamification/leaderboard");
        secondRequest.addHeader("If-None-Match", etag);
        MockHttpServletResponse secondResponse = new MockHttpServletResponse();
        filter.doFilter(secondRequest, secondResponse, new MockFilterChain(LEADERBOARD_SERVLET));

        assertThat(secondResponse.getStatus()).isEqualTo(304);
        assertThat(secondResponse.getContentAsString()).isEmpty();
    }

    // ── Edge case: stale ETag → 200 with new ETag ─────────────────────────────

    @Test
    void secondRequest_WithStaleEtag_ShouldReturn200_WithNewEtag() throws Exception {
        String staleEtag = "\"00000000000000000000000000000000\"";

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/gamification/leaderboard");
        request.addHeader("If-None-Match", staleEtag);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain(UPDATED_LEADERBOARD_SERVLET));

        assertThat(response.getStatus()).isEqualTo(200);
        String newEtag = response.getHeader("ETag");
        assertThat(newEtag).isNotNull().isNotEqualTo(staleEtag);
    }

    // ── Edge case: binary endpoint bypassed → no ETag header ─────────────────

    @Test
    void imageContentEndpoint_ShouldNotHaveEtagHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/images/42/content");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain(IMAGE_SERVLET));

        // Filter was bypassed — no ETag header should be set
        assertThat(response.getHeader("ETag")).isNull();
        assertThat(response.getStatus()).isEqualTo(200);
    }
}
