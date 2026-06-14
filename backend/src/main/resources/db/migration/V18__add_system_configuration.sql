-- V18: Add system_configuration and config_audit_log tables
-- Purpose: Allow superadmins to adjust malicious-labeling and fraud-detection
--          parameters at runtime without redeployment.
--          All changes are recorded in config_audit_log for auditing.

CREATE TABLE IF NOT EXISTS system_configuration (
    id           BIGSERIAL    PRIMARY KEY,
    config_key   VARCHAR(100) NOT NULL UNIQUE,
    config_value VARCHAR(500) NOT NULL,
    description  VARCHAR(500),
    updated_at   TIMESTAMP    DEFAULT NOW(),
    updated_by   VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS config_audit_log (
    id             BIGSERIAL    PRIMARY KEY,
    config_key     VARCHAR(100) NOT NULL,
    previous_value VARCHAR(500),
    new_value      VARCHAR(500) NOT NULL,
    changed_by     VARCHAR(255) NOT NULL,
    changed_at     TIMESTAMP    DEFAULT NOW()
);

CREATE INDEX idx_config_audit_log_key        ON config_audit_log (config_key);
CREATE INDEX idx_config_audit_log_changed_at ON config_audit_log (changed_at DESC);

-- Seed defaults that mirror the current application.yml values so existing
-- behaviour is preserved on first boot after the migration.
INSERT INTO system_configuration (config_key, config_value, description) VALUES
    ('credibility.malicious_threshold',
     '15.0',
     'Composite credibility score (0-100) below which a user is flagged as a potential malicious labeler'),

    ('credibility.malicious_min_samples',
     '20',
     'Minimum number of total classifications a user must have before malicious-labeling detection activates'),

    ('fraud.auto_ban_enabled',
     'true',
     'When true, users are automatically banned once their cumulative strike count reaches the ban threshold; when false, strikes and warnings still accumulate but no ban is issued'),

    ('fraud.min_response_time_ms',
     '300',
     'Minimum acceptable response time in milliseconds for regular users; faster responses are treated as suspicious'),

    ('fraud.researcher_min_response_time_ms',
     '150',
     'Minimum acceptable response time in milliseconds for researchers (domain experts legitimately swipe faster)'),

    ('fraud.suspicious_count_for_strike',
     '3',
     'Number of suspicious events that must occur within the sliding window before one formal STRIKE is recorded'),

    ('fraud.sliding_window_minutes',
     '10',
     'Duration of the sliding window (in minutes) used to count recent suspicious events'),

    ('fraud.strikes_for_warning_1',
     '5',
     'Cumulative strike count at which the first user warning is issued'),

    ('fraud.strikes_for_warning_2',
     '10',
     'Cumulative strike count at which the second (final) user warning is issued'),

    ('fraud.strikes_for_ban',
     '15',
     'Cumulative strike count at which the automatic ban is triggered (when auto_ban_enabled is true)'),

    ('fraud.warning_cooldown_minutes',
     '30',
     'Minimum minutes that must pass between consecutive warnings to prevent warning spam in a single bad session')

ON CONFLICT (config_key) DO NOTHING;
