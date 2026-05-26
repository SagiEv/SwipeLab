package com.swipelab.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PlatformOverviewResponse {

    private ActivitySummary today;
    private ActivitySummary thisWeek;
    private ActivitySummary thisMonth;

    // Daily average labeler credibility for last 30 days
    private List<ConfidenceTrendPoint> confidenceTrend;

    // YES / NO / DONT_KNOW / TRASH breakdown for last 30 days
    private List<LabelDistributionPoint> labelDistribution;

    private PlatformTotals totals;

    @Data
    @Builder
    public static class ActivitySummary {
        private long classifications;
        private long uniqueImages;
        private long uniqueUsers;
        private long uniqueTasks;
        private long uniqueExperiments;
    }

    @Data
    @Builder
    public static class ConfidenceTrendPoint {
        private String date;
        private double averageCredibility;
        private long classificationCount;
    }

    @Data
    @Builder
    public static class LabelDistributionPoint {
        private String label;
        private long count;
        private double percentage;
    }

    @Data
    @Builder
    public static class PlatformTotals {
        private long totalUsers;
        private long totalClassifications;
        private long totalImages;
        private long activeTasks;
    }
}
