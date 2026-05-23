package com.swipelab.gamification.domain;

/**
 * Tier progression driven by the total number of YES tags a user has submitted.
 * Thresholds: BEGINNER=1, EXPERT=50, PRO=200, LEGEND=1000.
 */
public enum RankTier {
    UNRANKED(0),
    BEGINNER(1),
    EXPERT(50),
    PRO(200),
    LEGEND(1000);

    /** Minimum yes_tag_count required to reach this tier. */
    public final int threshold;

    RankTier(int threshold) {
        this.threshold = threshold;
    }
}
