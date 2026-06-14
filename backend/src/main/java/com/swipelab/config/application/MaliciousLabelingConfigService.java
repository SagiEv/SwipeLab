package com.swipelab.config.application;

import com.swipelab.config.application.dto.ConfigAuditLogResponse;
import com.swipelab.config.application.dto.MaliciousLabelingConfigResponse;
import com.swipelab.config.application.dto.UpdateMaliciousLabelingConfigRequest;
import com.swipelab.config.domain.ConfigAuditLog;
import com.swipelab.config.domain.SystemConfiguration;
import com.swipelab.config.infrastructure.ConfigAuditLogRepository;
import com.swipelab.config.infrastructure.SystemConfigurationRepository;
import com.swipelab.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Manages runtime-adjustable parameters for malicious-labeling detection
 * and fraud-detection (including the auto-ban toggle).
 *
 * Design:
 *  - Parameters live in the system_configuration DB table (V18 migration).
 *  - getMaliciousLabelingConfig() is @Cacheable so every hot classification
 *    path reads from memory, not the DB.
 *  - updateConfig() is @CacheEvict — it clears the cache atomically within
 *    the same transaction that persists the new values, so callers always
 *    see either the old or the new config, never a torn read.
 *  - Every write is also appended to config_audit_log for full auditability.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaliciousLabelingConfigService {

    static final String CACHE_NAME = "maliciousLabelingConfig";
    static final String CACHE_KEY  = "'current'";   // single-entry cache

    private final SystemConfigurationRepository configRepository;
    private final ConfigAuditLogRepository      auditRepository;

    // ── Keys ──────────────────────────────────────────────────────────────────

    private static final String KEY_MALICIOUS_THRESHOLD             = "credibility.malicious_threshold";
    private static final String KEY_MALICIOUS_MIN_SAMPLES           = "credibility.malicious_min_samples";
    private static final String KEY_AUTO_BAN_ENABLED                = "fraud.auto_ban_enabled";
    private static final String KEY_MIN_RESPONSE_TIME_MS            = "fraud.min_response_time_ms";
    private static final String KEY_RESEARCHER_MIN_RESPONSE_TIME_MS = "fraud.researcher_min_response_time_ms";
    private static final String KEY_SUSPICIOUS_COUNT_FOR_STRIKE     = "fraud.suspicious_count_for_strike";
    private static final String KEY_SLIDING_WINDOW_MINUTES          = "fraud.sliding_window_minutes";
    private static final String KEY_STRIKES_FOR_WARNING_1           = "fraud.strikes_for_warning_1";
    private static final String KEY_STRIKES_FOR_WARNING_2           = "fraud.strikes_for_warning_2";
    private static final String KEY_STRIKES_FOR_BAN                 = "fraud.strikes_for_ban";
    private static final String KEY_WARNING_COOLDOWN_MINUTES        = "fraud.warning_cooldown_minutes";

    // ── Read ──────────────────────────────────────────────────────────────────

    /**
     * Returns the current configuration snapshot.
     * Result is cached under CACHE_NAME / 'current'; evicted on every update.
     */
    @Cacheable(value = CACHE_NAME, key = CACHE_KEY)
    @Transactional(readOnly = true)
    public MaliciousLabelingConfigResponse getMaliciousLabelingConfig() {
        log.debug("Loading malicious-labeling config from database");
        return MaliciousLabelingConfigResponse.builder()
                .maliciousThreshold(           getDouble(KEY_MALICIOUS_THRESHOLD,             15.0))
                .maliciousMinSamples(          getInt(   KEY_MALICIOUS_MIN_SAMPLES,            20))
                .autoBanEnabled(               getBool(  KEY_AUTO_BAN_ENABLED,                 true))
                .minResponseTimeMs(            getLong(  KEY_MIN_RESPONSE_TIME_MS,             300L))
                .researcherMinResponseTimeMs(  getLong(  KEY_RESEARCHER_MIN_RESPONSE_TIME_MS,  150L))
                .suspiciousCountForStrike(     getInt(   KEY_SUSPICIOUS_COUNT_FOR_STRIKE,      3))
                .slidingWindowMinutes(         getInt(   KEY_SLIDING_WINDOW_MINUTES,           10))
                .strikesForWarning1(           getInt(   KEY_STRIKES_FOR_WARNING_1,            5))
                .strikesForWarning2(           getInt(   KEY_STRIKES_FOR_WARNING_2,            10))
                .strikesForBan(                getInt(   KEY_STRIKES_FOR_BAN,                  15))
                .warningCooldownMinutes(       getInt(   KEY_WARNING_COOLDOWN_MINUTES,         30))
                .build();
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    /**
     * Applies a partial update — only non-null fields in the request are written.
     * Validates the full resulting config (cross-field ordering constraints) before
     * persisting anything, so the DB is never left in a half-written invalid state.
     *
     * @param request   fields to change (null = keep existing value)
     * @param changedBy username of the superadmin making the change (audit trail)
     * @return the new config snapshot after all changes are applied
     */
    @CacheEvict(value = CACHE_NAME, key = CACHE_KEY)
    @Transactional
    public MaliciousLabelingConfigResponse updateConfig(
            UpdateMaliciousLabelingConfigRequest request, String changedBy) {

        // Build a merged view of old + requested new values for cross-field validation.
        MaliciousLabelingConfigResponse current = loadCurrentRaw();
        validate(request, current);

        // Persist each changed field and append an audit record.
        if (request.getMaliciousThreshold() != null) {
            updateKey(KEY_MALICIOUS_THRESHOLD,
                    String.valueOf(current.getMaliciousThreshold()),
                    String.valueOf(request.getMaliciousThreshold()),
                    changedBy);
        }
        if (request.getMaliciousMinSamples() != null) {
            updateKey(KEY_MALICIOUS_MIN_SAMPLES,
                    String.valueOf(current.getMaliciousMinSamples()),
                    String.valueOf(request.getMaliciousMinSamples()),
                    changedBy);
        }
        if (request.getAutoBanEnabled() != null) {
            updateKey(KEY_AUTO_BAN_ENABLED,
                    String.valueOf(current.isAutoBanEnabled()),
                    String.valueOf(request.getAutoBanEnabled()),
                    changedBy);
        }
        if (request.getMinResponseTimeMs() != null) {
            updateKey(KEY_MIN_RESPONSE_TIME_MS,
                    String.valueOf(current.getMinResponseTimeMs()),
                    String.valueOf(request.getMinResponseTimeMs()),
                    changedBy);
        }
        if (request.getResearcherMinResponseTimeMs() != null) {
            updateKey(KEY_RESEARCHER_MIN_RESPONSE_TIME_MS,
                    String.valueOf(current.getResearcherMinResponseTimeMs()),
                    String.valueOf(request.getResearcherMinResponseTimeMs()),
                    changedBy);
        }
        if (request.getSuspiciousCountForStrike() != null) {
            updateKey(KEY_SUSPICIOUS_COUNT_FOR_STRIKE,
                    String.valueOf(current.getSuspiciousCountForStrike()),
                    String.valueOf(request.getSuspiciousCountForStrike()),
                    changedBy);
        }
        if (request.getSlidingWindowMinutes() != null) {
            updateKey(KEY_SLIDING_WINDOW_MINUTES,
                    String.valueOf(current.getSlidingWindowMinutes()),
                    String.valueOf(request.getSlidingWindowMinutes()),
                    changedBy);
        }
        if (request.getStrikesForWarning1() != null) {
            updateKey(KEY_STRIKES_FOR_WARNING_1,
                    String.valueOf(current.getStrikesForWarning1()),
                    String.valueOf(request.getStrikesForWarning1()),
                    changedBy);
        }
        if (request.getStrikesForWarning2() != null) {
            updateKey(KEY_STRIKES_FOR_WARNING_2,
                    String.valueOf(current.getStrikesForWarning2()),
                    String.valueOf(request.getStrikesForWarning2()),
                    changedBy);
        }
        if (request.getStrikesForBan() != null) {
            updateKey(KEY_STRIKES_FOR_BAN,
                    String.valueOf(current.getStrikesForBan()),
                    String.valueOf(request.getStrikesForBan()),
                    changedBy);
        }
        if (request.getWarningCooldownMinutes() != null) {
            updateKey(KEY_WARNING_COOLDOWN_MINUTES,
                    String.valueOf(current.getWarningCooldownMinutes()),
                    String.valueOf(request.getWarningCooldownMinutes()),
                    changedBy);
        }

        log.info("Malicious-labeling config updated by {}", changedBy);

        // Return the freshly persisted state (cache was just evicted by @CacheEvict).
        return loadCurrentRaw();
    }

    // ── Audit log ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<ConfigAuditLogResponse> getAuditLog(Pageable pageable) {
        return auditRepository.findAllByOrderByChangedAtDesc(pageable)
                .map(this::toAuditResponse);
    }

    // ── Validation ────────────────────────────────────────────────────────────

    /**
     * Cross-field validation of the merged (existing + new) parameter set.
     * Throws IllegalArgumentException with a descriptive message on any violation.
     */
    private void validate(UpdateMaliciousLabelingConfigRequest req,
                          MaliciousLabelingConfigResponse current) {

        double threshold   = req.getMaliciousThreshold()          != null ? req.getMaliciousThreshold()          : current.getMaliciousThreshold();
        int    minSamples  = req.getMaliciousMinSamples()          != null ? req.getMaliciousMinSamples()          : current.getMaliciousMinSamples();
        long   minRespTime = req.getMinResponseTimeMs()            != null ? req.getMinResponseTimeMs()            : current.getMinResponseTimeMs();
        long   resMinResp  = req.getResearcherMinResponseTimeMs()  != null ? req.getResearcherMinResponseTimeMs()  : current.getResearcherMinResponseTimeMs();
        int    suspCount   = req.getSuspiciousCountForStrike()     != null ? req.getSuspiciousCountForStrike()     : current.getSuspiciousCountForStrike();
        int    window      = req.getSlidingWindowMinutes()         != null ? req.getSlidingWindowMinutes()         : current.getSlidingWindowMinutes();
        int    warn1       = req.getStrikesForWarning1()           != null ? req.getStrikesForWarning1()           : current.getStrikesForWarning1();
        int    warn2       = req.getStrikesForWarning2()           != null ? req.getStrikesForWarning2()           : current.getStrikesForWarning2();
        int    ban         = req.getStrikesForBan()                != null ? req.getStrikesForBan()                : current.getStrikesForBan();
        int    cooldown    = req.getWarningCooldownMinutes()       != null ? req.getWarningCooldownMinutes()       : current.getWarningCooldownMinutes();

        if (threshold < 0.0 || threshold > 100.0)
            throw new IllegalArgumentException("maliciousThreshold must be in [0.0, 100.0]");
        if (minSamples < 1 || minSamples > 10000)
            throw new IllegalArgumentException("maliciousMinSamples must be in [1, 10000]");
        if (minRespTime < 50 || minRespTime > 10000)
            throw new IllegalArgumentException("minResponseTimeMs must be in [50, 10000]");
        if (resMinResp < 50 || resMinResp > 10000)
            throw new IllegalArgumentException("researcherMinResponseTimeMs must be in [50, 10000]");
        if (resMinResp > minRespTime)
            throw new IllegalArgumentException(
                    "researcherMinResponseTimeMs (" + resMinResp + ") must be ≤ minResponseTimeMs (" + minRespTime + ")");
        if (suspCount < 1 || suspCount > 100)
            throw new IllegalArgumentException("suspiciousCountForStrike must be in [1, 100]");
        if (window < 1 || window > 1440)
            throw new IllegalArgumentException("slidingWindowMinutes must be in [1, 1440]");
        if (warn1 < 1)
            throw new IllegalArgumentException("strikesForWarning1 must be ≥ 1");
        if (warn2 <= warn1)
            throw new IllegalArgumentException(
                    "strikesForWarning2 (" + warn2 + ") must be > strikesForWarning1 (" + warn1 + ")");
        if (ban <= warn2)
            throw new IllegalArgumentException(
                    "strikesForBan (" + ban + ") must be > strikesForWarning2 (" + warn2 + ")");
        if (ban > 1000)
            throw new IllegalArgumentException("strikesForBan must be ≤ 1000");
        if (cooldown < 1 || cooldown > 1440)
            throw new IllegalArgumentException("warningCooldownMinutes must be in [1, 1440]");
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Loads the current config directly from the DB (bypassing the cache).
     * Used internally: before validation (to merge with request) and after
     * all writes (to return the freshly persisted state).
     */
    private MaliciousLabelingConfigResponse loadCurrentRaw() {
        return MaliciousLabelingConfigResponse.builder()
                .maliciousThreshold(           getDouble(KEY_MALICIOUS_THRESHOLD,             15.0))
                .maliciousMinSamples(          getInt(   KEY_MALICIOUS_MIN_SAMPLES,            20))
                .autoBanEnabled(               getBool(  KEY_AUTO_BAN_ENABLED,                 true))
                .minResponseTimeMs(            getLong(  KEY_MIN_RESPONSE_TIME_MS,             300L))
                .researcherMinResponseTimeMs(  getLong(  KEY_RESEARCHER_MIN_RESPONSE_TIME_MS,  150L))
                .suspiciousCountForStrike(     getInt(   KEY_SUSPICIOUS_COUNT_FOR_STRIKE,      3))
                .slidingWindowMinutes(         getInt(   KEY_SLIDING_WINDOW_MINUTES,           10))
                .strikesForWarning1(           getInt(   KEY_STRIKES_FOR_WARNING_1,            5))
                .strikesForWarning2(           getInt(   KEY_STRIKES_FOR_WARNING_2,            10))
                .strikesForBan(                getInt(   KEY_STRIKES_FOR_BAN,                  15))
                .warningCooldownMinutes(       getInt(   KEY_WARNING_COOLDOWN_MINUTES,         30))
                .build();
    }

    /** Persists a single key's new value and appends an audit record. */
    private void updateKey(String key, String previousValue, String newValue, String changedBy) {
        SystemConfiguration entry = configRepository.findByConfigKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Config key not found: " + key));

        entry.setConfigValue(newValue);
        entry.setUpdatedBy(changedBy);
        entry.setUpdatedAt(LocalDateTime.now());
        configRepository.save(entry);

        auditRepository.save(ConfigAuditLog.builder()
                .configKey(key)
                .previousValue(previousValue)
                .newValue(newValue)
                .changedBy(changedBy)
                .build());

        log.info("Config updated: key={} | {} → {} | by={}", key, previousValue, newValue, changedBy);
    }

    private double getDouble(String key, double fallback) {
        return configRepository.findByConfigKey(key)
                .map(c -> Double.parseDouble(c.getConfigValue()))
                .orElse(fallback);
    }

    private int getInt(String key, int fallback) {
        return configRepository.findByConfigKey(key)
                .map(c -> Integer.parseInt(c.getConfigValue()))
                .orElse(fallback);
    }

    private long getLong(String key, long fallback) {
        return configRepository.findByConfigKey(key)
                .map(c -> Long.parseLong(c.getConfigValue()))
                .orElse(fallback);
    }

    private boolean getBool(String key, boolean fallback) {
        return configRepository.findByConfigKey(key)
                .map(c -> Boolean.parseBoolean(c.getConfigValue()))
                .orElse(fallback);
    }

    private ConfigAuditLogResponse toAuditResponse(ConfigAuditLog log) {
        return ConfigAuditLogResponse.builder()
                .id(log.getId())
                .configKey(log.getConfigKey())
                .previousValue(log.getPreviousValue())
                .newValue(log.getNewValue())
                .changedBy(log.getChangedBy())
                .changedAt(log.getChangedAt())
                .build();
    }
}
