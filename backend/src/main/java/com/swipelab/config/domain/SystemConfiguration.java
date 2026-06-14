package com.swipelab.config.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Runtime-adjustable system parameter stored in the database.
 *
 * Why not application.yml?  @Value-injected fields are resolved once at startup;
 * changing them requires a full redeployment.  By persisting parameters here,
 * superadmins can adjust malicious-labeling and fraud-detection thresholds live
 * without any service disruption.
 *
 * Values are loaded by MaliciousLabelingConfigService (Caffeine-cached, evicted
 * on every successful update) so read cost is negligible.
 *
 * Schema: V18__add_system_configuration.sql
 */
@Entity
@Table(
    name = "system_configuration",
    indexes = {
        @Index(name = "idx_syscfg_key", columnList = "config_key", unique = true)
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Dot-separated namespaced key, e.g. "credibility.malicious_threshold". */
    @Column(name = "config_key", nullable = false, unique = true, length = 100)
    private String configKey;

    /** Stored as plain text; type coercion is done in the application layer. */
    @Column(name = "config_value", nullable = false, length = 500)
    private String configValue;

    /** Human-readable explanation of what the parameter controls. */
    @Column(name = "description", length = 500)
    private String description;

    /** Automatically set to the current time whenever the row is saved. */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Username of the superadmin who last changed this value. */
    @Column(name = "updated_by", length = 255)
    private String updatedBy;
}
