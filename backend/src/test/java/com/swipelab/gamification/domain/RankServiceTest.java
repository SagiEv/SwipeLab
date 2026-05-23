package com.swipelab.gamification.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RankServiceTest {

    private final RankService rankService = new RankService();

    // ── computeRank ──────────────────────────────────────────────────────────

    @Test
    void computeRank_ShouldReturnUnranked_WhenYesTagCountIsZero() {
        assertThat(rankService.computeRank(0)).isEqualTo(RankTier.UNRANKED);
    }

    @Test
    void computeRank_ShouldReturnBeginner_WhenYesTagCountIsOne() {
        assertThat(rankService.computeRank(1)).isEqualTo(RankTier.BEGINNER);
    }

    @Test
    void computeRank_ShouldReturnExpert_WhenAtExpertThreshold() {
        assertThat(rankService.computeRank(50)).isEqualTo(RankTier.EXPERT);
    }

    @Test
    void computeRank_ShouldReturnPro_WhenAtProThreshold() {
        assertThat(rankService.computeRank(200)).isEqualTo(RankTier.PRO);
    }

    @Test
    void computeRank_ShouldReturnLegend_WhenAtLegendThreshold() {
        assertThat(rankService.computeRank(1000)).isEqualTo(RankTier.LEGEND);
    }

    @Test
    void computeRank_ShouldReturnLegend_WhenAboveLegendThreshold() {
        assertThat(rankService.computeRank(9999)).isEqualTo(RankTier.LEGEND);
    }

    @Test
    void computeRank_ShouldReturnBeginner_WhenJustBelowExpert() {
        assertThat(rankService.computeRank(49)).isEqualTo(RankTier.BEGINNER);
    }

    // ── nextTierThreshold ────────────────────────────────────────────────────

    @Test
    void nextTierThreshold_ShouldReturnBeginnerThreshold_WhenUnranked() {
        assertThat(rankService.nextTierThreshold(0)).isEqualTo(RankTier.BEGINNER.threshold);
    }

    @Test
    void nextTierThreshold_ShouldReturnNegativeOne_WhenAlreadyLegend() {
        assertThat(rankService.nextTierThreshold(1000)).isEqualTo(-1);
    }

    @Test
    void nextTierThreshold_ShouldReturnProThreshold_WhenExpert() {
        assertThat(rankService.nextTierThreshold(50)).isEqualTo(RankTier.PRO.threshold);
    }

    // ── progressPercent ──────────────────────────────────────────────────────

    @Test
    void progressPercent_ShouldReturn100_WhenLegend() {
        assertThat(rankService.progressPercent(1000)).isEqualTo(100);
    }

    @Test
    void progressPercent_ShouldReturn0_WhenAtExactTierStart() {
        // At EXPERT start (50), progress toward PRO (200) is 0%
        assertThat(rankService.progressPercent(50)).isEqualTo(0);
    }

    @Test
    void progressPercent_ShouldReturn50_WhenHalfwayToNextTier() {
        // Halfway between EXPERT (50) and PRO (200) → at 125 → 50%
        assertThat(rankService.progressPercent(125)).isEqualTo(50);
    }
}
