package com.swipelab.integration.stardbi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalCropDto {
    @JsonProperty("box_id")
    private Long boxId;

    @JsonProperty("image_id")
    private Long imageId;

    @JsonProperty("species_id")
    private Long speciesId;
}
