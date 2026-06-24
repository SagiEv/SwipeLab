package com.swipelab.integration.stardbi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/v1/system")
@RequiredArgsConstructor
public class SyncController {

    private final StardbiSyncService stardbiSyncService;

    @PreAuthorize("@securityAuthorizationService.isSuperAdmin(authentication.name)")
    @PostMapping("/sync-stardbi")
    public ResponseEntity<String> forceSyncStardbi() {
        log.info("Manual Stardbi sync triggered via REST API");
        // Run asynchronously so the HTTP request doesn't timeout during long downloads
        CompletableFuture.runAsync(stardbiSyncService::syncExperiments)
            .exceptionally(ex -> {
                log.error("Failed manual async sync", ex);
                return null;
            });
            
        return ResponseEntity.accepted().body("Stardbi synchronization has been accepted and started in the background.");
    }
}
