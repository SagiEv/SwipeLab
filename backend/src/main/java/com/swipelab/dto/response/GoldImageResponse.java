package com.swipelab.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoldImageResponse {
    private Long id;
    private Long imageId;
    private String imageUrl;
    private String caption;
    private Long taskId;
    private Long correctLabelId;
    private String correctLabelName;
    private String difficultyLevel;
    private String explanation;
    private LocalDateTime createdAt;
}
