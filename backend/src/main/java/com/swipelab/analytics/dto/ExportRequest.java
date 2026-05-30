package com.swipelab.analytics.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExportRequest {
    @NotEmpty(message = "At least one task ID must be provided")
    private List<Long> taskIds;
}
