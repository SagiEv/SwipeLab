package com.swipelab.config.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swipelab.config.application.MaliciousLabelingConfigService;
import com.swipelab.config.application.dto.ConfigAuditLogResponse;
import com.swipelab.config.application.dto.MaliciousLabelingConfigResponse;
import com.swipelab.config.application.dto.UpdateMaliciousLabelingConfigRequest;
import com.swipelab.auth.application.SecurityAuthorizationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MaliciousLabelingConfigController.class)
@DisplayName("MaliciousLabelingConfigController")
class MaliciousLabelingConfigControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private MaliciousLabelingConfigService configService;
    @MockBean private SecurityAuthorizationService securityAuthorizationService;

    private MaliciousLabelingConfigResponse defaultConfig() {
        return MaliciousLabelingConfigResponse.builder()
                .maliciousThreshold(15.0)
                .maliciousMinSamples(20)
                .autoBanEnabled(true)
                .minResponseTimeMs(300L)
                .researcherMinResponseTimeMs(150L)
                .suspiciousCountForStrike(3)
                .slidingWindowMinutes(10)
                .strikesForWarning1(5)
                .strikesForWarning2(10)
                .strikesForBan(15)
                .warningCooldownMinutes(30)
                .build();
    }

    // ── Happy flows ───────────────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "superadmin", roles = {"RESEARCHER"})
    @DisplayName("GET /api/admin/malicious-labeling-config → 200 with full config")
    void getConfig_superAdmin_returns200() throws Exception {
        when(securityAuthorizationService.isSuperAdmin("superadmin")).thenReturn(true);
        when(configService.getMaliciousLabelingConfig()).thenReturn(defaultConfig());

        mockMvc.perform(get("/api/admin/malicious-labeling-config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maliciousThreshold").value(15.0))
                .andExpect(jsonPath("$.autoBanEnabled").value(true))
                .andExpect(jsonPath("$.strikesForBan").value(15));
    }

    @Test
    @WithMockUser(username = "superadmin", roles = {"RESEARCHER"})
    @DisplayName("PUT /api/admin/malicious-labeling-config → 200 with updated config")
    void updateConfig_superAdmin_returns200() throws Exception {
        when(securityAuthorizationService.isSuperAdmin("superadmin")).thenReturn(true);

        MaliciousLabelingConfigResponse updated = defaultConfig();
        // Simulate auto-ban toggled off
        when(configService.updateConfig(any(UpdateMaliciousLabelingConfigRequest.class), anyString()))
                .thenReturn(MaliciousLabelingConfigResponse.builder()
                        .maliciousThreshold(15.0).maliciousMinSamples(20)
                        .autoBanEnabled(false)
                        .minResponseTimeMs(300L).researcherMinResponseTimeMs(150L)
                        .suspiciousCountForStrike(3).slidingWindowMinutes(10)
                        .strikesForWarning1(5).strikesForWarning2(10)
                        .strikesForBan(15).warningCooldownMinutes(30)
                        .build());

        UpdateMaliciousLabelingConfigRequest req = new UpdateMaliciousLabelingConfigRequest();
        req.setAutoBanEnabled(false);

        mockMvc.perform(put("/api/admin/malicious-labeling-config")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autoBanEnabled").value(false));
    }

    @Test
    @WithMockUser(username = "superadmin", roles = {"RESEARCHER"})
    @DisplayName("GET /api/admin/malicious-labeling-config/audit-log → 200 with page")
    void getAuditLog_superAdmin_returns200() throws Exception {
        when(securityAuthorizationService.isSuperAdmin("superadmin")).thenReturn(true);

        ConfigAuditLogResponse entry = ConfigAuditLogResponse.builder()
                .id(1L).configKey("fraud.auto_ban_enabled")
                .previousValue("true").newValue("false")
                .changedBy("superadmin").changedAt(LocalDateTime.now())
                .build();

        Page<ConfigAuditLogResponse> page = new PageImpl<>(List.of(entry));
        when(configService.getAuditLog(any())).thenReturn(page);

        mockMvc.perform(get("/api/admin/malicious-labeling-config/audit-log"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].configKey").value("fraud.auto_ban_enabled"))
                .andExpect(jsonPath("$.content[0].changedBy").value("superadmin"));
    }

    // ── Authorization edge cases ──────────────────────────────────────────────

    @Test
    @WithMockUser(username = "regularuser", roles = {"USER"})
    @DisplayName("GET /api/admin/malicious-labeling-config → 403 for non-superadmin")
    void getConfig_nonSuperAdmin_returns403() throws Exception {
        when(securityAuthorizationService.isSuperAdmin("regularuser")).thenReturn(false);

        mockMvc.perform(get("/api/admin/malicious-labeling-config"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(configService);
    }

    @Test
    @WithMockUser(username = "researcher1", roles = {"RESEARCHER"})
    @DisplayName("PUT /api/admin/malicious-labeling-config → 403 for researcher (non-superadmin)")
    void updateConfig_researcher_returns403() throws Exception {
        when(securityAuthorizationService.isSuperAdmin("researcher1")).thenReturn(false);

        UpdateMaliciousLabelingConfigRequest req = new UpdateMaliciousLabelingConfigRequest();
        req.setMaliciousThreshold(20.0);

        mockMvc.perform(put("/api/admin/malicious-labeling-config")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(configService);
    }

    @Test
    @WithMockUser(username = "superadmin", roles = {"RESEARCHER"})
    @DisplayName("PUT with invalid threshold → 400 with error message")
    void updateConfig_invalidThreshold_returns400() throws Exception {
        when(securityAuthorizationService.isSuperAdmin("superadmin")).thenReturn(true);
        when(configService.updateConfig(any(), anyString()))
                .thenThrow(new IllegalArgumentException("maliciousThreshold must be in [0.0, 100.0]"));

        UpdateMaliciousLabelingConfigRequest req = new UpdateMaliciousLabelingConfigRequest();
        req.setMaliciousThreshold(999.0);

        mockMvc.perform(put("/api/admin/malicious-labeling-config")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/admin/malicious-labeling-config → 401 when unauthenticated")
    void getConfig_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/malicious-labeling-config"))
                .andExpect(status().isUnauthorized());
    }
}
