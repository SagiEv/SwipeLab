package com.swipelab.config.application.dto;

import lombok.Data;

/**
 * Partial-update request for malicious-labeling and fraud-detection config.
 *
 * Every field is nullable (boxed type / wrapper).  Only non-null fields are
 * written to the database; the rest retain their current values.
 * Validation is enforced in MaliciousLabelingConfigService before any write.
 *
 * Sent to PUT /api/admin/malicious-labeling-config.
 */
@Data
public class UpdateMaliciousLabelingConfigRequest {

    // ── Credibility-based detection ────────────────────────────────────────────

    /** Must be in [0.0, 100.0]. */
    private Double maliciousThreshold;

    /** Must be in [1, 10000]. */
    private Integer maliciousMinSamples;

    // ── Fraud detection (speed-based) ─────────────────────────────────────────

    private Boolean autoBanEnabled;

    /** Must be in [50, 10000] and ≥ researcherMinResponseTimeMs when both provided. */
    private Long minResponseTimeMs;

    /** Must be in [50, 10000] and ≤ minResponseTimeMs. */
    private Long researcherMinResponseTimeMs;

    /** Must be in [1, 100]. */
    private Integer suspiciousCountForStrike;

    /** Must be in [1, 1440]. */
    private Integer slidingWindowMinutes;

    /** Must be ≥ 1 and < strikesForWarning2. */
    private Integer strikesForWarning1;

    /** Must be > strikesForWarning1 and < strikesForBan. */
    private Integer strikesForWarning2;

    /** Must be > strikesForWarning2 and ≤ 1000. */
    private Integer strikesForBan;

    /** Must be in [1, 1440]. */
    private Integer warningCooldownMinutes;
}
