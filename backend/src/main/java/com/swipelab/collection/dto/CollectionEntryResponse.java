package com.swipelab.collection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionEntryResponse {
    private Long id;
    private Long imageId;
    private String species;
    private String imageUrl;
    private Long taskId;
    private LocalDateTime taggedAt;
}
