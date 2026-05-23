package com.swipelab.gamification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response payload for GET /api/v1/gamification/rank.
 * Carries everything the frontend needs to render a rank badge and progress bar.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankResponse {
    /** Current tier label (e.g., "EXPERT"). */
    private String tier;
    /** Total YES tags submitted so far. */
    private int yesTagCount;
    /** YES tags required to reach the next tier; -1 when already at LEGEND. */
    private int nextTierAt;
    /** Progress toward the next tier as 0–100 percent. */
    private int progressPercent;
}
