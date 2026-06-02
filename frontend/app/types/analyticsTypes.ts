// TypeScript interfaces matching the backend analytics DTOs (Issue #220 / #221)

// ─── Platform Overview (GET /api/v1/analytics/overview) ──────────────────────

export interface ActivitySummary {
  classifications: number;
  uniqueImages: number;
  uniqueUsers: number;
  uniqueTasks: number;
  uniqueExperiments: number;
}

export interface ConfidenceTrendPoint {
  date: string;              // "YYYY-MM-DD"
  averageCredibility: number; // 0–100 (backend AVG of credibility_at_time column)
  classificationCount: number;
}

export interface LabelDistributionPoint {
  label: string;    // "YES" | "NO" | "DONT_KNOW" | "TRASH"
  count: number;
  percentage: number;
}

export interface PlatformTotals {
  totalUsers: number;
  totalClassifications: number;
  totalImages: number;
  activeTasks: number;
}

export interface PlatformOverview {
  today: ActivitySummary;
  thisWeek: ActivitySummary;
  thisMonth: ActivitySummary;
  confidenceTrend: ConfidenceTrendPoint[];
  labelDistribution: LabelDistributionPoint[];
  totals: PlatformTotals;
}

// ─── Global Stats (GET /api/v1/analytics/global-stats) ───────────────────────

export interface GlobalStats {
  totalUsers: number;
  totalSwipes: number;
  activeTasks: number;
  totalImages: number;
}

// ─── Task Analytics (GET /api/v1/analytics/tasks/:id) ────────────────────────

export interface TaskAnalyticsProgress {
  totalImages: number;
  imagesClassified: number;
  completedImages: number;
  percentComplete: number;
}

export interface TaskAnalyticsConsensus {
  overallAverage: number | null;
  lowConsensusImages: number | null;
  threshold: number | null;
}

export interface TaskAnalyticsParticipation {
  activeUsers: number | null;
  totalClassifications: number | null;
  averageClassificationsPerUser: number | null;
  medianResponseTimeMs: number | null;
}

export interface TaskAnalyticsQuality {
  averageCredibility: number | null;
  expertAgreement: number | null;
  lowQualityUsers: number | null;
}

export interface TaskAnalytics {
  taskId: number;
  taskName?: string;
  status: string;
  progress?: TaskAnalyticsProgress;
  consensus?: TaskAnalyticsConsensus;
  participation?: TaskAnalyticsParticipation;
  quality?: TaskAnalyticsQuality;
  generatedAt?: string;
}

// ─── User Performance (GET /api/v1/analytics/top-performers) ─────────────────

export interface UserPerformance {
  username: string;
  displayName?: string;
  totalClassifications: number;
  goldImageClassifications?: number;
  correctGoldClassifications?: number;
  goldAccuracy: number;
  credibilityScore: number; // composite 0–100 (40% gold + 35% majority + 25% expert kappa)
  currentStreak?: number;
  points?: number;
}
