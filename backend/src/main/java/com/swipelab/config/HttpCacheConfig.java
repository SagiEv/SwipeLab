package com.swipelab.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

/**
 * Registers the Spring ShallowEtagHeaderFilter.
 *
 * The filter computes an MD5 hash of each response body, sets the ETag response header,
 * and returns 304 Not Modified when the client sends a matching If-None-Match request header.
 *
 * Binary content endpoints are excluded to prevent buffering large payloads in memory:
 *   - /api/v1/images/{id}/content     (Stardbi image crops — can be megabytes)
 *   - /api/admin/gold-images/{id}/image (gold-standard images)
 *
 * Cache-Control headers are applied separately by CacheControlInterceptor.
 */
@Configuration
public class HttpCacheConfig {

    @Bean
    public FilterRegistrationBean<ShallowEtagHeaderFilter> shallowEtagHeaderFilter() {
        ShallowEtagHeaderFilter filter = new ShallowEtagHeaderFilter() {
            @Override
            protected boolean shouldNotFilter(HttpServletRequest request) {
                String path = request.getRequestURI();
                // Skip ETag computation for binary image endpoints to avoid
                // buffering the full image body in memory.
                return path.matches(".*/images/[^/]+/content")
                        || path.matches(".*/gold-images/[^/]+/image");
            }
        };

        FilterRegistrationBean<ShallowEtagHeaderFilter> registration = new FilterRegistrationBean<>(filter);
        registration.addUrlPatterns("/*");
        registration.setName("shallowEtagHeaderFilter");
        registration.setOrder(1);
        return registration;
    }
}
