package com.swipelab.config.infrastructure;

import com.swipelab.config.domain.SystemConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SystemConfigurationRepository extends JpaRepository<SystemConfiguration, Long> {

    /** Load a single parameter by its dot-namespaced key (e.g. "fraud.auto_ban_enabled"). */
    Optional<SystemConfiguration> findByConfigKey(String configKey);

    /**
     * Load all parameters whose key starts with the given prefix.
     * Used to bulk-fetch a namespace, e.g. findByConfigKeyStartingWith("fraud.")
     * returns every fraud-detection parameter in one query.
     */
    List<SystemConfiguration> findByConfigKeyStartingWith(String prefix);
}
