package com.swipelab.collection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionStatsResponse {
    /** Total number of YES-tagged images in this user's collection. */
    private long total;
}
