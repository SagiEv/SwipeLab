package com.swipelab.config.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Read-only snapshot of all malicious-labeling and fraud-detection parameters.
 * Returned by GET /api/admin/malicious-labeling-config.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaliciousLabelingConfigResponse {

    // ── Credibility-based detection ────────────────────────────────────────────

    /** Composite score (0-100) below which a user is flagged as a potential malicious labeler. */
    private double maliciousThreshold;

    /** Minimum total classifications before the malicious-labeling check activates. */
    private int maliciousMinSamples;

    // ── Fraud detection (speed-based) ─────────────────────────────────────────

    /** When false, strikes and warnings accumulate but no automatic ban is issued. */
    private boolean autoBanEnabled;

    /** Minimum acceptable response time (ms) for regular users. */
    private long minResponseTimeMs;

    /** Minimum acceptable response time (ms) for researchers. */
    private long researcherMinResponseTimeMs;

    /** Suspicious events within the sliding window needed to record one formal strike. */
    private int suspiciousCountForStrike;

    /** Duration of the sliding window in minutes. */
    private int slidingWindowMinutes;

    /** Cumulative strikes to trigger the first warning. */
    private int strikesForWarning1;

    /** Cumulative strikes to trigger the second (final) warning. */
    private int strikesForWarning2;

    /** Cumulative strikes to trigger automatic ban (when autoBanEnabled is true). */
    private int strikesForBan;

    /** Minimum minutes between consecutive warnings (prevents warning spam). */
    private int warningCooldownMinutes;
}
