package com.swipelab.config.infrastructure;

import com.swipelab.config.domain.ConfigAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigAuditLogRepository extends JpaRepository<ConfigAuditLog, Long> {

    /** Paginated audit history for a specific config key, newest first. */
    Page<ConfigAuditLog> findByConfigKeyOrderByChangedAtDesc(String configKey, Pageable pageable);

    /** Full paginated audit history across all keys, newest first. */
    Page<ConfigAuditLog> findAllByOrderByChangedAtDesc(Pageable pageable);
}
