package com.swipelab.config.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Single audit-log entry returned by GET /api/admin/malicious-labeling-config/audit-log.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigAuditLogResponse {

    private Long id;

    /** The key that was changed, e.g. "fraud.auto_ban_enabled". */
    private String configKey;

    /** Value before the change; null when the key was first created. */
    private String previousValue;

    /** Value after the change. */
    private String newValue;

    /** Username of the superadmin who made the change. */
    private String changedBy;

    /** Timestamp of the change. */
    private LocalDateTime changedAt;
}
