package com.swipelab.config.api;

import com.swipelab.config.application.MaliciousLabelingConfigService;
import com.swipelab.config.application.dto.ConfigAuditLogResponse;
import com.swipelab.config.application.dto.MaliciousLabelingConfigResponse;
import com.swipelab.config.application.dto.UpdateMaliciousLabelingConfigRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Superadmin-only REST API for managing malicious-labeling and fraud-detection
 * configuration parameters at runtime.
 *
 * All endpoints require Super Admin authentication — non-superadmin requests are
 * rejected with 403 at the method-security layer before any business logic runs.
 *
 * GET  /api/admin/malicious-labeling-config            – current parameter snapshot
 * PUT  /api/admin/malicious-labeling-config            – partial or full update
 * GET  /api/admin/malicious-labeling-config/audit-log  – paginated change history
 */
@RestController
@RequestMapping("/api/admin/malicious-labeling-config")
@PreAuthorize("@securityAuthorizationService.isSuperAdmin(authentication.name)")
@RequiredArgsConstructor
public class MaliciousLabelingConfigController {

    private final MaliciousLabelingConfigService configService;

    // ── GET current config ────────────────────────────────────────────────────

    /**
     * Returns a snapshot of all current malicious-labeling and fraud-detection
     * parameters.  Response is served from the Caffeine cache on subsequent calls.
     *
     * @return 200 with {@link MaliciousLabelingConfigResponse}
     */
    @GetMapping
    public ResponseEntity<MaliciousLabelingConfigResponse> getConfig() {
        return ResponseEntity.ok(configService.getMaliciousLabelingConfig());
    }

    // ── PUT update config ─────────────────────────────────────────────────────

    /**
     * Partially or fully updates malicious-labeling / fraud-detection parameters.
     *
     * Only non-null fields in the request body are written; omitted fields retain
     * their current values.  Cross-field ordering constraints are validated before
     * any DB write — a violation returns 400 with a descriptive message.
     *
     * Every change is appended to the config_audit_log table (key, before, after,
     * who, when) and the in-memory cache is evicted synchronously within the same
     * transaction.
     *
     * @param request    partial or complete set of parameters to update
     * @param userDetails resolved from the JWT — used to record the actor in the audit log
     * @return 200 with the updated {@link MaliciousLabelingConfigResponse}
     * @throws IllegalArgumentException (→ 400) when any validation rule is violated
     */
    @PutMapping
    public ResponseEntity<MaliciousLabelingConfigResponse> updateConfig(
            @RequestBody UpdateMaliciousLabelingConfigRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String changedBy = userDetails.getUsername();
        MaliciousLabelingConfigResponse updated = configService.updateConfig(request, changedBy);
        return ResponseEntity.ok(updated);
    }

    // ── GET audit log ─────────────────────────────────────────────────────────

    /**
     * Returns a paginated list of all configuration changes, newest first.
     *
     * Query params:
     *   page – zero-based page index (default 0)
     *   size – entries per page (default 20, max 100)
     *
     * @return 200 with a {@link Page} of {@link ConfigAuditLogResponse}
     */
    @GetMapping("/audit-log")
    public ResponseEntity<Page<ConfigAuditLogResponse>> getAuditLog(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        int cappedSize = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, cappedSize, Sort.by("changedAt").descending());
        return ResponseEntity.ok(configService.getAuditLog(pageable));
    }
}
