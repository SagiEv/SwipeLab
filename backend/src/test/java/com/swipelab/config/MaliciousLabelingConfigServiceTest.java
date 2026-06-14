package com.swipelab.config.application;

import com.swipelab.config.application.dto.MaliciousLabelingConfigResponse;
import com.swipelab.config.application.dto.UpdateMaliciousLabelingConfigRequest;
import com.swipelab.config.domain.ConfigAuditLog;
import com.swipelab.config.domain.SystemConfiguration;
import com.swipelab.config.infrastructure.ConfigAuditLogRepository;
import com.swipelab.config.infrastructure.SystemConfigurationRepository;
import com.swipelab.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MaliciousLabelingConfigService")
class MaliciousLabelingConfigServiceTest {

    @Mock private SystemConfigurationRepository configRepository;
    @Mock private ConfigAuditLogRepository auditRepository;

    @InjectMocks
    private MaliciousLabelingConfigService configService;

    // ── Helpers ───────────────────────────────────────────────────────────────

    private SystemConfiguration entry(String key, String value) {
        return SystemConfiguration.builder()
                .id(1L)
                .configKey(key)
                .configValue(value)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private void stubAllKeys() {
        lenient().when(configRepository.findByConfigKey("credibility.malicious_threshold"))
                .thenReturn(Optional.of(entry("credibility.malicious_threshold", "15.0")));
        lenient().when(configRepository.findByConfigKey("credibility.malicious_min_samples"))
                .thenReturn(Optional.of(entry("credibility.malicious_min_samples", "20")));
        lenient().when(configRepository.findByConfigKey("fraud.auto_ban_enabled"))
                .thenReturn(Optional.of(entry("fraud.auto_ban_enabled", "true")));
        lenient().when(configRepository.findByConfigKey("fraud.min_response_time_ms"))
                .thenReturn(Optional.of(entry("fraud.min_response_time_ms", "300")));
        lenient().when(configRepository.findByConfigKey("fraud.researcher_min_response_time_ms"))
                .thenReturn(Optional.of(entry("fraud.researcher_min_response_time_ms", "150")));
        lenient().when(configRepository.findByConfigKey("fraud.suspicious_count_for_strike"))
                .thenReturn(Optional.of(entry("fraud.suspicious_count_for_strike", "3")));
        lenient().when(configRepository.findByConfigKey("fraud.sliding_window_minutes"))
                .thenReturn(Optional.of(entry("fraud.sliding_window_minutes", "10")));
        lenient().when(configRepository.findByConfigKey("fraud.strikes_for_warning_1"))
                .thenReturn(Optional.of(entry("fraud.strikes_for_warning_1", "5")));
        lenient().when(configRepository.findByConfigKey("fraud.strikes_for_warning_2"))
                .thenReturn(Optional.of(entry("fraud.strikes_for_warning_2", "10")));
        lenient().when(configRepository.findByConfigKey("fraud.strikes_for_ban"))
                .thenReturn(Optional.of(entry("fraud.strikes_for_ban", "15")));
        lenient().when(configRepository.findByConfigKey("fraud.warning_cooldown_minutes"))
                .thenReturn(Optional.of(entry("fraud.warning_cooldown_minutes", "30")));
    }

    // ── Happy flows ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("getMaliciousLabelingConfig: returns correct values from DB")
    void getConfig_returnsAllValuesFromDb() {
        stubAllKeys();

        MaliciousLabelingConfigResponse cfg = configService.getMaliciousLabelingConfig();

        assertThat(cfg.getMaliciousThreshold()).isEqualTo(15.0);
        assertThat(cfg.getMaliciousMinSamples()).isEqualTo(20);
        assertThat(cfg.isAutoBanEnabled()).isTrue();
        assertThat(cfg.getMinResponseTimeMs()).isEqualTo(300L);
        assertThat(cfg.getResearcherMinResponseTimeMs()).isEqualTo(150L);
        assertThat(cfg.getSuspiciousCountForStrike()).isEqualTo(3);
        assertThat(cfg.getSlidingWindowMinutes()).isEqualTo(10);
        assertThat(cfg.getStrikesForWarning1()).isEqualTo(5);
        assertThat(cfg.getStrikesForWarning2()).isEqualTo(10);
        assertThat(cfg.getStrikesForBan()).isEqualTo(15);
        assertThat(cfg.getWarningCooldownMinutes()).isEqualTo(30);
    }

    @Test
    @DisplayName("updateConfig: single field — persists new value and appends audit record")
    void updateConfig_singleField_persistsAndAudits() {
        stubAllKeys();

        SystemConfiguration thresholdEntry = entry("credibility.malicious_threshold", "15.0");
        when(configRepository.findByConfigKey("credibility.malicious_threshold"))
                .thenReturn(Optional.of(thresholdEntry));
        when(configRepository.save(any())).thenReturn(thresholdEntry);
        when(auditRepository.save(any())).thenReturn(new ConfigAuditLog());

        UpdateMaliciousLabelingConfigRequest req = new UpdateMaliciousLabelingConfigRequest();
        req.setMaliciousThreshold(20.0);

        configService.updateConfig(req, "superadmin");

        ArgumentCaptor<SystemConfiguration> savedConfig = ArgumentCaptor.forClass(SystemConfiguration.class);
        verify(configRepository, atLeastOnce()).save(savedConfig.capture());
        assertThat(savedConfig.getValue().getConfigValue()).isEqualTo("20.0");
        assertThat(savedConfig.getValue().getUpdatedBy()).isEqualTo("superadmin");

        ArgumentCaptor<ConfigAuditLog> auditCaptor = ArgumentCaptor.forClass(ConfigAuditLog.class);
        verify(auditRepository, atLeastOnce()).save(auditCaptor.capture());
        assertThat(auditCaptor.getValue().getConfigKey()).isEqualTo("credibility.malicious_threshold");
        assertThat(auditCaptor.getValue().getPreviousValue()).isEqualTo("15.0");
        assertThat(auditCaptor.getValue().getNewValue()).isEqualTo("20.0");
        assertThat(auditCaptor.getValue().getChangedBy()).isEqualTo("superadmin");
    }

    @Test
    @DisplayName("updateConfig: auto-ban toggled to false — persisted and audited")
    void updateConfig_autoBanToggle_persistsAndAudits() {
        stubAllKeys();

        SystemConfiguration banEntry = entry("fraud.auto_ban_enabled", "true");
        when(configRepository.findByConfigKey("fraud.auto_ban_enabled"))
                .thenReturn(Optional.of(banEntry));
        when(configRepository.save(any())).thenReturn(banEntry);
        when(auditRepository.save(any())).thenReturn(new ConfigAuditLog());

        UpdateMaliciousLabelingConfigRequest req = new UpdateMaliciousLabelingConfigRequest();
        req.setAutoBanEnabled(false);

        configService.updateConfig(req, "superadmin");

        ArgumentCaptor<ConfigAuditLog> auditCaptor = ArgumentCaptor.forClass(ConfigAuditLog.class);
        verify(auditRepository, atLeastOnce()).save(auditCaptor.capture());
        assertThat(auditCaptor.getValue().getConfigKey()).isEqualTo("fraud.auto_ban_enabled");
        assertThat(auditCaptor.getValue().getNewValue()).isEqualTo("false");
    }

    @Test
    @DisplayName("getAuditLog: returns paginated audit entries newest-first")
    void getAuditLog_returnsPaginatedEntries() {
        ConfigAuditLog log = ConfigAuditLog.builder()
                .id(1L).configKey("fraud.auto_ban_enabled")
                .previousValue("true").newValue("false")
                .changedBy("admin").changedAt(LocalDateTime.now()).build();

        Page<ConfigAuditLog> page = new PageImpl<>(List.of(log));
        when(auditRepository.findAllByOrderByChangedAtDesc(any())).thenReturn(page);

        Page<com.swipelab.config.application.dto.ConfigAuditLogResponse> result =
                configService.getAuditLog(PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getConfigKey()).isEqualTo("fraud.auto_ban_enabled");
        assertThat(result.getContent().get(0).getPreviousValue()).isEqualTo("true");
        assertThat(result.getContent().get(0).getNewValue()).isEqualTo("false");
        assertThat(result.getContent().get(0).getChangedBy()).isEqualTo("admin");
    }

    // ── Validation edge cases ─────────────────────────────────────────────────

    @Test
    @DisplayName("updateConfig: maliciousThreshold > 100 → IllegalArgumentException, nothing saved")
    void updateConfig_thresholdOutOfRange_throws() {
        stubAllKeys();

        UpdateMaliciousLabelingConfigRequest req = new UpdateMaliciousLabelingConfigRequest();
        req.setMaliciousThreshold(101.0);

        assertThatThrownBy(() -> configService.updateConfig(req, "superadmin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maliciousThreshold");

        verify(configRepository, never()).save(any());
        verify(auditRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateConfig: researcherMinResponseTime > minResponseTime → rejected")
    void updateConfig_researcherTimeExceedsRegular_throws() {
        stubAllKeys();

        UpdateMaliciousLabelingConfigRequest req = new UpdateMaliciousLabelingConfigRequest();
        req.setMinResponseTimeMs(200L);
        req.setResearcherMinResponseTimeMs(500L); // exceeds regular threshold

        assertThatThrownBy(() -> configService.updateConfig(req, "superadmin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("researcherMinResponseTimeMs");

        verify(configRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateConfig: strikesForWarning2 ≤ strikesForWarning1 → rejected")
    void updateConfig_warning2SmallerThanWarning1_throws() {
        stubAllKeys();

        UpdateMaliciousLabelingConfigRequest req = new UpdateMaliciousLabelingConfigRequest();
        req.setStrikesForWarning1(10);
        req.setStrikesForWarning2(5); // must be > warning1

        assertThatThrownBy(() -> configService.updateConfig(req, "superadmin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("strikesForWarning2");

        verify(configRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateConfig: strikesForBan ≤ strikesForWarning2 → rejected")
    void updateConfig_banSmallerThanWarning2_throws() {
        stubAllKeys();

        UpdateMaliciousLabelingConfigRequest req = new UpdateMaliciousLabelingConfigRequest();
        req.setStrikesForBan(8); // current warning2 = 10 → ban must be > 10

        assertThatThrownBy(() -> configService.updateConfig(req, "superadmin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("strikesForBan");

        verify(configRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateConfig: missing DB key → ResourceNotFoundException propagated")
    void updateConfig_missingDbKey_throwsResourceNotFound() {
        stubAllKeys();
        // Override: threshold key exists but save path will hit a missing key
        SystemConfiguration thresholdEntry = entry("credibility.malicious_threshold", "15.0");
        when(configRepository.findByConfigKey("credibility.malicious_threshold"))
                .thenReturn(Optional.of(thresholdEntry))
                .thenReturn(Optional.empty()); // second call (after cache evict) returns empty

        // Use a separate scenario: directly missing key on update path
        when(configRepository.findByConfigKey("credibility.malicious_threshold"))
                .thenReturn(Optional.empty());

        UpdateMaliciousLabelingConfigRequest req = new UpdateMaliciousLabelingConfigRequest();
        req.setMaliciousThreshold(20.0);

        // validation needs current config → stub the remaining keys still via lenient above
        assertThatThrownBy(() -> configService.updateConfig(req, "superadmin"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
