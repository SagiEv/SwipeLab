package com.swipelab.config.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Immutable audit record of every superadmin configuration change.
 *
 * A new row is appended (never updated) each time a system_configuration value
 * is modified via MaliciousLabelingConfigService.  This satisfies the requirement
 * to record: who made the change, when, what the previous value was, and what
 * it was changed to.
 *
 * Schema: V18__add_system_configuration.sql
 */
@Entity
@Table(
    name = "config_audit_log",
    indexes = {
        @Index(name = "idx_config_audit_log_key",        columnList = "config_key"),
        @Index(name = "idx_config_audit_log_changed_at", columnList = "changed_at")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The key that was changed, e.g. "fraud.auto_ban_enabled". */
    @Column(name = "config_key", nullable = false, length = 100)
    private String configKey;

    /** The value before the change; null if the key is being created for the first time. */
    @Column(name = "previous_value", length = 500)
    private String previousValue;

    /** The value after the change. */
    @Column(name = "new_value", nullable = false, length = 500)
    private String newValue;

    /** Username of the superadmin who triggered the change. */
    @Column(name = "changed_by", nullable = false, length = 255)
    private String changedBy;

    /** Timestamp set by the database on insert; never updated. */
    @CreationTimestamp
    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;
}
