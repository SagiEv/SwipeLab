package com.swipelab.dto.response;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class TaskAnalyticsResponse {
    private Long taskId;
    private String status;
//    private ProgressAnalytics progress;
//    private ConsensusAnalytics consensus;
//    private List<SpeciesAnalytics> speciesAnalytics;
}
