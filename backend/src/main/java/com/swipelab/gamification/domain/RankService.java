package com.swipelab.gamification.domain;

import org.springframework.stereotype.Service;

/**
 * Pure domain service — stateless rank computation.
 * No DB access; called by GamificationOrchestratorService after persisting the updated yes_tag_count.
 */
@Service
public class RankService {

    private static final RankTier[] TIERS = RankTier.values();

    /**
     * Returns the highest tier the user has reached based on their total YES tag count.
     */
    public RankTier computeRank(int yesTagCount) {
        RankTier current = RankTier.UNRANKED;
        for (RankTier tier : TIERS) {
            if (yesTagCount >= tier.threshold) {
                current = tier;
            }
        }
        return current;
    }

    /**
     * Returns the threshold of the next tier above the user's current one,
     * or -1 if the user is already at the highest tier.
     */
    public int nextTierThreshold(int yesTagCount) {
        RankTier current = computeRank(yesTagCount);
        for (RankTier tier : TIERS) {
            if (tier.threshold > current.threshold) {
                return tier.threshold;
            }
        }
        return -1; // already LEGEND
    }

    /**
     * Progress percentage toward the next tier (0–100).
     * Returns 100 if already at LEGEND.
     */
    public int progressPercent(int yesTagCount) {
        RankTier current = computeRank(yesTagCount);
        int next = nextTierThreshold(yesTagCount);
        if (next == -1) return 100;
        int base = current.threshold;
        return (int) (((double) (yesTagCount - base) / (next - base)) * 100);
    }
}
