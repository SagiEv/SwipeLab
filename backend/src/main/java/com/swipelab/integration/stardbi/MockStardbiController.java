package com.swipelab.integration.stardbi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Base64;
import java.time.Instant;

@Slf4j
@RestController
@RequestMapping("/stardbi")
@Profile({"mock", "e2e"})
public class MockStardbiController {

    @PostMapping("/auth/get_token/")
    public ResponseEntity<Object> getToken(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");
        log.info("Mock Stardbi login for {}", username);
        
        String header = Base64.getUrlEncoder().withoutPadding().encodeToString("{\"alg\":\"none\",\"typ\":\"JWT\"}".getBytes());
        String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(String.format(
            "{\"user_id\":1,\"username\":\"%s\",\"exp\":%d}",
            username != null ? username : "mockuser",
            Instant.now().plusSeconds(3600).getEpochSecond()
        ).getBytes());
        String mockAccessToken = header + "." + payload + ".mock-signature";
        String mockRefreshToken = header + "." + payload + ".mock-refresh-sig";

        return ResponseEntity.ok(Map.of(
                "access", mockAccessToken,
                "refresh", mockRefreshToken,
                "lifetime", 3600,
                "id", 1L,
                "username", username != null ? username : "mockuser",
                "first_name", "Mock",
                "last_name", "Researcher",
                "email", (username != null ? username : "mockuser") + "@stardbi.external"
        ));
    }

    @GetMapping("/auth/check_auth/")
    public ResponseEntity<Void> checkAuth(@RequestHeader("Authorization") String authHeader) {
        log.info("Mock Stardbi check auth");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/auth/token_refresh/")
    public ResponseEntity<Object> refreshToken(@RequestBody Map<String, Object> request) {
        log.info("Mock Stardbi refresh token");
        String header = Base64.getUrlEncoder().withoutPadding().encodeToString("{\"alg\":\"none\",\"typ\":\"JWT\"}".getBytes());
        String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(String.format(
            "{\"user_id\":1,\"username\":\"%s\",\"exp\":%d}",
            "mockuser",
            Instant.now().plusSeconds(3600).getEpochSecond()
        ).getBytes());
        String mockAccessToken = header + "." + payload + ".mock-signature";
        String mockRefreshToken = header + "." + payload + ".mock-refresh-sig";

        return ResponseEntity.ok(Map.of(
                "access", mockAccessToken,
                "refresh", mockRefreshToken,
                "lifetime", 3600
        ));
    }

    @PostMapping("/auth/logout/")
    public ResponseEntity<Void> logout(@RequestBody Map<String, Object> request, @RequestHeader("Authorization") String authHeader) {
        log.info("Mock Stardbi logout");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/swipe_lab/experiments/")
    public ResponseEntity<Object> getExperiments() {
        log.info("Mock Stardbi get experiments");
        return ResponseEntity.ok(List.of(
                Map.of(
                        "id", 1L,
                        "name", "Mock Experiment 1",
                        "startDate", "2025-01-01",
                        "emdDate", "2025-12-31",
                        "notes", "Mock experiment notes"
                ),
                Map.of(
                        "id", 2L,
                        "name", "Mock Experiment 2",
                        "startDate", "2026-01-01",
                        "emdDate", "2026-12-31",
                        "notes", "Another mock experiment"
                )
        ));
    }

    @GetMapping("/swipe_lab/crops/")
    public ResponseEntity<Object> getCrops(@RequestParam("experiment") Long experimentId) {
        log.info("Mock Stardbi get crops for experiment {}", experimentId);
        return ResponseEntity.ok(List.of(
                Map.of("box_id", 101L, "image_id", 201L, "species_id", 301L),
                Map.of("box_id", 102L, "image_id", 201L, "species_id", 301L),
                Map.of("box_id", 103L, "image_id", 202L, "species_id", 302L),
                Map.of("box_id", 104L, "image_id", 202L, "species_id", 302L),
                Map.of("box_id", 105L, "image_id", 203L, "species_id", 303L)
        ));
    }

    @GetMapping("/swipe_lab/crops/{id}/image/")
    public ResponseEntity<Resource> getCropImage(@PathVariable("id") Long id) {
        log.info("Mock Stardbi get crop image {}", id);
        try {
            // E2E mock crops are stored in e2e-crops, fallback to random IDs if specific image doesn't exist
            Path imagePath = Paths.get("src/main/resources/e2e-crops/1_201_" + id + ".png");
            MediaType mediaType = MediaType.IMAGE_PNG;
            
            if (!Files.exists(imagePath)) {
                imagePath = Paths.get("src/main/resources/e2e-crops/1_201_" + id + ".jpg");
                mediaType = MediaType.IMAGE_JPEG;
            }
            
            if (Files.exists(imagePath)) {
                Resource resource = new UrlResource(imagePath.toUri());
                return ResponseEntity.ok()
                        .contentType(mediaType)
                        .body(resource);
            }
            
            // Fallback so it never returns empty 404
            byte[] fallback = Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==");
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(new org.springframework.core.io.ByteArrayResource(fallback));
        } catch (Exception e) {
            log.warn("Could not load mock image {}", id, e);
        }
        
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/swipe_lab/crops/download/")
    public ResponseEntity<byte[]> downloadExperimentCropsZip(@RequestParam("experiment") Long experimentId) {
        log.info("Mock Stardbi download crops zip for experiment {}", experimentId);
        try (java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
             java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(baos)) {
            
            java.io.File folder = new java.io.File("src/main/resources/e2e-crops");
            boolean filesAdded = false;
            if (folder.exists() && folder.isDirectory()) {
                java.io.File[] files = folder.listFiles();
                if (files != null) {
                    for (java.io.File file : files) {
                        if (file.isFile() && (file.getName().endsWith(".jpg") || file.getName().endsWith(".png"))) {
                            try {
                                String nameWithoutExt = file.getName().substring(0, file.getName().lastIndexOf('.'));
                                String[] parts = nameWithoutExt.split("_");
                                if (parts.length >= 3) {
                                    Long boxId = Long.parseLong(parts[parts.length - 1]);
                                    Long parentImageId = Long.parseLong(String.join("", java.util.Arrays.copyOfRange(parts, 1, parts.length - 1)));
                                    long randomShift = (long)(Math.random() * 1000000);
                                    Long uniqueBoxId = boxId + (experimentId * 1000) + randomShift;
                                    String ext = file.getName().substring(file.getName().lastIndexOf('.'));
                                    java.util.zip.ZipEntry entry = new java.util.zip.ZipEntry(parentImageId + "_" + uniqueBoxId + ext);
                                    zos.putNextEntry(entry);
                                    zos.write(java.nio.file.Files.readAllBytes(file.toPath()));
                                    zos.closeEntry();
                                    filesAdded = true;
                                }
                            } catch (Exception e) {
                                log.warn("Could not parse image name for zip: {}", file.getName());
                            }
                        }
                    }
                }
            }
            if (!filesAdded) {
                for (long i = 1; i <= 5; i++) {
                    java.util.zip.ZipEntry entry = new java.util.zip.ZipEntry((i * 100) + "_" + (i * 10) + ".png");
                    zos.putNextEntry(entry);
                    byte[] fallback = Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==");
                    zos.write(fallback);
                    zos.closeEntry();
                }
            }
            zos.finish();
            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"experiment_" + experimentId + "_crops.zip\"")
                    .contentType(org.springframework.http.MediaType.valueOf("application/zip"))
                    .body(baos.toByteArray());
        } catch (Exception e) {
            log.error("Error generating mock zip", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/swipe_lab/taxonomy/")
    public ResponseEntity<Object> getTaxonomy() {
        log.info("Mock Stardbi get taxonomy");
        return ResponseEntity.ok(List.of(
                Map.of(
                        "specis_id", 1L, // Must match the ExternalTaxonomyDto exact typo "specis_id"
                        "clazz", "insecta", // Notice it maps to clazz or class depending on how the DTO is written. We will use `class` if possible, but map.of won't care. Wait: Jackson usually serializes map string as is. DTO might annotate it as @JsonProperty("class").
                        "class", "insecta", 
                        "order", "Lepidoptera",
                        "family", "Nymphalidae",
                        "genus", "Danaus",
                        "species", "D. plexippus"
                ),
                Map.of(
                        "specis_id", 2L,
                        "class", "insecta",
                        "order", "Coleoptera",
                        "family", "Coccinellidae",
                        "genus", "Harmonia",
                        "species", "H. axyridis"
                )
        ));
    }

    @PostMapping("/swipe_lab/labels/")
    public ResponseEntity<Object> postLabel(@RequestBody Map<String, Object> label) {
        log.info("[MockStardbi] Label received → box_id={}, image_id={}, species_id={}, user={}, grade={}",
                label.get("box_id"), label.get("image_id"), label.get("species_id"),
                label.get("swipe_lab_user_id"), label.get("user_grade"));
        return ResponseEntity.status(201).body(Map.of("label_id", 1L));
    }
}
