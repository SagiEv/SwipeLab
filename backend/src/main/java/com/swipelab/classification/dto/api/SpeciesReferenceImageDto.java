package com.swipelab.classification.dto.api;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/** Returned by the species reference-image API endpoints. */
@Data
@Builder
public class SpeciesReferenceImageDto {
    private Long          id;
    private Long          labelId;
    /** Full compressed image URL (served via /api/v1/species/reference-images/{id}/image). */
    private String        imageUrl;
    /** 200px thumbnail URL (served via /api/v1/species/reference-images/{id}/thumbnail). */
    private String        thumbnailUrl;
    private Long          fileSizeBytes;
    private String        caption;
    private String        uploadedBy;
    private LocalDateTime createdAt;
}
