package com.swipelab.config;

import com.swipelab.classification.events.ClassificationSubmittedEvent;
import com.swipelab.gamification.events.GamificationUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Centralized cache eviction listener.
 *
 * Hooks into the existing Spring event system to invalidate stale cache entries
 * after mutations. Keeps eviction logic out of business services — they publish
 * events and this listener handles the cache consequences.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheEvictionListener {

    private final CacheManager cacheManager;

    // ── Classification submitted ───────────────────────────────────────────────

    /**
     * A classification changes: user's gamification score, collection stats,
     * profile credibility, leaderboard ranking, and platform overview totals.
     */
    @EventListener
    public void onClassificationSubmitted(ClassificationSubmittedEvent event) {
        String username = event.getUsername();
        if (username == null) {
            log.warn("ClassificationSubmittedEvent received with null username — skipping cache eviction.");
            return;
        }

        // Per-user caches
        evict(CacheConfig.CACHE_GAMIFICATION, username);
        evict(CacheConfig.CACHE_COLLECTION_STATS, username);
        evict(CacheConfig.CACHE_USER_PROFILE, username);

        // Shared caches
        evictAll(CacheConfig.CACHE_LEADERBOARD);
        evictAll(CacheConfig.CACHE_PLATFORM_OVERVIEW);

        log.debug("Cache evicted after classification by user={}", username);
    }

    // ── Gamification updated ───────────────────────────────────────────────────

    /**
     * Handles explicit gamification updates (badge awards, score adjustments)
     * published separately from the classification event flow.
     */
    @EventListener
    public void onGamificationUpdated(GamificationUpdatedEvent event) {
        String username = event.getUsername();
        if (username == null) {
            log.warn("GamificationUpdatedEvent received with null username — skipping cache eviction.");
            return;
        }

        evict(CacheConfig.CACHE_GAMIFICATION, username);
        evict(CacheConfig.CACHE_USER_PROFILE, username);

        log.debug("Cache evicted after gamification update for user={}", username);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    /** Evicts a single key from a named cache. No-op if the cache does not exist. */
    private void evict(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        }
    }

    /** Clears every entry from a named cache. No-op if the cache does not exist. */
    private void evictAll(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }
}
