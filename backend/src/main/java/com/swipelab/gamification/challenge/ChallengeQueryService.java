package com.swipelab.gamification.challenge;

import com.swipelab.gamification.badge.BadgeDefinition;
import com.swipelab.gamification.badge.BadgeDefinitionRepository;
import com.swipelab.gamification.domain.Gamification;
import com.swipelab.gamification.dto.BadgeDto;
import com.swipelab.gamification.dto.ChallengeDto;
import com.swipelab.gamification.dto.UserBadgeDto;
import com.swipelab.gamification.infrastructure.GamificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChallengeQueryService {

    private final ChallengeDefinitionRepository challengeDefinitionRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final BadgeDefinitionRepository badgeDefinitionRepository;
    private final GamificationRepository gamificationRepository;

    /**
     * Catalog of the badges awarded by {@link com.swipelab.gamification.domain.BadgeService}
     * (stored as a CSV in {@code Gamification.badge}). Keyed by badge title so we can hydrate
     * each earned title with its description + icon. Mirrors the V6 migration metadata.
     */
    private record BadgeMeta(String description, String iconUrl) {}

    private static final Map<String, BadgeMeta> BADGE_CATALOG = Map.of(
            "First Swipe",  new BadgeMeta("Awarded for completing your first classification", "badge_first_swipe.png"),
            "10 Swipes",    new BadgeMeta("Awarded for completing 10 classifications",         "badge_10_swipes.png"),
            "100 Swipes",   new BadgeMeta("Awarded for completing 100 classifications",        "badge_100_swipes.png"),
            "3 Day Streak", new BadgeMeta("Awarded for maintaining a 3-day streak",            "badge_streak_3.png"),
            "7 Day Streak", new BadgeMeta("Awarded for maintaining a 7-day streak",            "badge_streak_7.png"),
            "1000 Points",  new BadgeMeta("Awarded for earning 1000 points",                   "badge_points_1000.png")
    );

    @Transactional(readOnly = true)
    public List<ChallengeDto> getActiveChallengesForUser(String username) {
        LocalDateTime now = LocalDateTime.now();
        List<ChallengeDefinition> activeDefs = challengeDefinitionRepository.findActiveChallenges(now);
        
        // Cache badges for fast lookup
        Map<java.util.UUID, BadgeDefinition> badgeMap = badgeDefinitionRepository.findAll().stream()
                .collect(Collectors.toMap(BadgeDefinition::getId, b -> b));

        // Group user challenges by definitionid, picking the most recent or active one
        // For simplicity, we fetch all and match
        List<UserChallenge> userChallenges = userChallengeRepository.findByUsername(username);

        return activeDefs.stream().map(def -> {
            // Find active user challenge for this def
            UserChallenge currentUserChallenge = userChallenges.stream()
                    .filter(uc -> uc.getDefinition().getId().equals(def.getId()) &&
                            uc.getWindowStart().isBefore(now) &&
                            uc.getWindowEnd().isAfter(now))
                    .findFirst()
                    .orElse(null);

            int progress = currentUserChallenge != null ? currentUserChallenge.getCurrentProgress() : 0;
            boolean completed = currentUserChallenge != null && currentUserChallenge.isCompleted();
            LocalDateTime start = currentUserChallenge != null ? currentUserChallenge.getWindowStart() : null;
            LocalDateTime end = currentUserChallenge != null ? currentUserChallenge.getWindowEnd() : null;
            
            BadgeDefinition badgeDef = badgeMap.get(def.getBadgeId());

            return ChallengeDto.builder()
                    .challengeId(def.getId())
                    .name(def.getName())
                    .description(def.getDescription())
                    .progress(progress)
                    .target(def.getTargetValue())
                    .completed(completed)
                    .windowStart(start)
                    .windowEnd(end)
                    .badge(badgeDef != null ? BadgeDto.builder()
                            .title(badgeDef.getTitle())
                            .iconUrl(badgeDef.getIconUrl())
                            .build() : null)
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * Returns the badges a user has earned. Reads the live {@code Gamification.badge} CSV that
     * {@link com.swipelab.gamification.domain.BadgeService} writes on every classification, so the
     * same data drives the Profile screen in every Spring profile (prod / e2e) with no per-profile
     * seeding. Each earned title is hydrated from {@link #BADGE_CATALOG}; unknown titles still
     * surface (with no icon) rather than being dropped.
     */
    @Transactional(readOnly = true)
    public List<UserBadgeDto> getUserBadges(String username) {
        String csv = gamificationRepository.findById(username)
                .map(Gamification::getBadge)
                .orElse(null);

        if (csv == null || csv.isBlank()) {
            return List.of();
        }

        List<UserBadgeDto> badges = new ArrayList<>();
        for (String raw : csv.split(",")) {
            String title = raw.trim();
            if (title.isEmpty()) {
                continue;
            }
            BadgeMeta meta = BADGE_CATALOG.get(title);
            badges.add(UserBadgeDto.builder()
                    .title(title)
                    .description(meta != null ? meta.description() : "")
                    .iconUrl(meta != null ? meta.iconUrl() : "")
                    .earnedAt(null) // not tracked in the CSV store
                    .build());
        }
        return badges;
    }
}
