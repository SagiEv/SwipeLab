package com.swipelab.tasks.api;

import com.swipelab.integration.stardbi.StardbiClient;
import com.swipelab.integration.stardbi.dto.ExternalTaxonomyDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/metadata")
@RequiredArgsConstructor
public class MetadataController {

    private final StardbiClient stardbiClient;

    @GetMapping("/species")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ExternalTaxonomyDto>> getSpecies() {
        try {
            // Retrieves target species taxonomy directly from Stardbi
            List<ExternalTaxonomyDto> taxonomy = stardbiClient.getTaxonomy();
            return ResponseEntity.ok(taxonomy);
        } catch (Exception e) {
            log.error("SEVERE: Failed to fetch species metadata", e);
            throw e; 
        }
    }
}
