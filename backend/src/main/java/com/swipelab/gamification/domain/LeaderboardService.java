package com.swipelab.gamification.domain;

import com.swipelab.config.CacheConfig;
import com.swipelab.gamification.infrastructure.GamificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final GamificationRepository gamificationRepository;

    @Cacheable(value = CacheConfig.CACHE_LEADERBOARD, key = "#limit")
    public List<Gamification> getGlobalLeaderboard(int limit) {
        return gamificationRepository.findAll(
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "score"))).getContent();
    }

    public List<Gamification> getStreakLeaderboard(int limit) {
        return gamificationRepository.findAll(
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "currentStreak"))).getContent();
    }

    @Cacheable(value = CacheConfig.CACHE_GAMIFICATION, key = "#username")
    public Gamification getGamification(String username) {
        return gamificationRepository.findById(username)
                .orElse(Gamification.builder()
                        .username(username)
                        .score(0L)
                        .currentStreak(0)
                        .build());
    }
}
