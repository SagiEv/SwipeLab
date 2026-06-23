package com.swipelab.auth.infrastructure;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * IP-based rate limiter for authentication endpoints using Bucket4j
 * token-bucket algorithm backed by a Caffeine in-process cache.
 *
 * Why a filter and not an aspect/annotation?
 *  - Filters run before the DispatcherServlet, so they protect against
 *    abusive clients without consuming any Spring context resources.
 *  - This keeps the controllers clean and the limit logic centralised.
 *
 * Limits (configurable via the LIMITS map):
 *  - /login:            5 requests per minute
 *  - /register:         3 requests per 15 minutes
 *  - /password/forgot:  3 requests per hour  (email bombing prevention)
 *  - /email/resend:     3 requests per hour  (email bombing prevention)
 *  - /invitation/admin: 5 requests per hour
 *
 * Cache TTL: 2 hours — buckets are evicted after 2h of inactivity, so a
 * persistent attacker cannot indefinitely grow the cache.
 */
@Slf4j
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    /** Maps a URI suffix to a bandwidth limit definition. */
    private static final Map<String, Bandwidth> LIMITS = Map.of(
            "/api/v1/auth/login",
            Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1))),

            "/api/v1/auth/register",
            Bandwidth.classic(3, Refill.intervally(3, Duration.ofMinutes(15))),

            "/api/v1/auth/password/forgot",
            Bandwidth.classic(3, Refill.intervally(3, Duration.ofHours(1))),

            "/api/v1/auth/email/resend",
            Bandwidth.classic(3, Refill.intervally(3, Duration.ofHours(1))),

            "/api/v1/auth/invitation/admin",
            Bandwidth.classic(5, Refill.intervally(5, Duration.ofHours(1)))
    );

    /**
     * Cache key: "<client-ip>:<request-uri>".
     * TTL of 2 h ensures that an idle attacker's bucket is evicted while still
     * providing a generous window for legitimate session gaps.
     */
    private final Cache<String, Bucket> bucketCache = Caffeine.newBuilder()
            .expireAfterAccess(2, TimeUnit.HOURS)
            .maximumSize(10_000)
            .build();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        Bandwidth limit = LIMITS.get(uri);

        // Only rate-limit the endpoints listed in LIMITS; pass everything else through.
        if (limit == null) {
            chain.doFilter(request, response);
            return;
        }

        String clientIp = resolveClientIp(request);
        String cacheKey = clientIp + ":" + uri;

        Bucket bucket = bucketCache.get(cacheKey, k -> Bucket.builder()
                .addLimit(limit)
                .build());

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for IP={} on URI={}", clientIp, uri);
            rejectRequest(response);
        }
    }

    /**
     * Resolve the real client IP, respecting X-Forwarded-For when behind a
     * reverse proxy (Nginx / Docker / cloud load balancer).
     * Only the first IP in the forwarded chain is used to prevent spoofing via
     * a crafted multi-hop XFF header.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void rejectRequest(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                "{\"error\":\"Too Many Requests\"," +
                "\"message\":\"Rate limit exceeded. Please slow down and try again later.\"}"
        );
    }
}
