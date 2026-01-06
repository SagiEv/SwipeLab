export type TaskAnalytics = {
    taskId: number;
    taskName: string;
    status: string;
    totalImages: number;
    classifiedImages: number;
    completionPercentage: number;
    averageConsensus: number;
    lowConsensusCount: number;
    highConsensusCount: number;
    labelDistribution: Record<string, number>;
    totalClassifications: number;
    uniqueClassifiers: number;
};

export type UserPerformance = {
    username: string;
    displayName: string;
    totalClassifications: number;
    goldImageClassifications: number;
    correctGoldClassifications: number;
    goldAccuracy: number;
    credibilityScore: number;
    currentStreak: number;
    points: number;
};

const MOCK_TASK_ANALYTICS: TaskAnalytics = {
    taskId: 1,
    taskName: "Bee Species Classification",
    status: "ACTIVE",
    totalImages: 150,
    classifiedImages: 98,
    completionPercentage: 65.3,
    averageConsensus: 78.5,
    lowConsensusCount: 12,
    highConsensusCount: 68,
    labelDistribution: {
        "Honey Bee": 45,
        "Bumblebee": 32,
        "Wasp": 21,
    },
    totalClassifications: 456,
    uniqueClassifiers: 15,
};

const MOCK_USER_PERFORMANCE: UserPerformance[] = [
    {
        username: "user1",
        displayName: "Alice Johnson",
        totalClassifications: 89,
        goldImageClassifications: 12,
        correctGoldClassifications: 11,
        goldAccuracy: 91.7,
        credibilityScore: 0.92,
        currentStreak: 5,
        points: 450,
    },
    {
        username: "user2",
        displayName: "Bob Smith",
        totalClassifications: 76,
        goldImageClassifications: 10,
        correctGoldClassifications: 9,
        goldAccuracy: 90.0,
        credibilityScore: 0.89,
        currentStreak: 3,
        points: 380,
    },
    {
        username: "user3",
        displayName: "Carol Davis",
        totalClassifications: 65,
        goldImageClassifications: 8,
        correctGoldClassifications: 7,
        goldAccuracy: 87.5,
        credibilityScore: 0.86,
        currentStreak: 2,
        points: 325,
    },
    {
        username: "user4",
        displayName: "David Lee",
        totalClassifications: 58,
        goldImageClassifications: 7,
        correctGoldClassifications: 6,
        goldAccuracy: 85.7,
        credibilityScore: 0.84,
        currentStreak: 1,
        points: 290,
    },
    {
        username: "user5",
        displayName: "Emma Wilson",
        totalClassifications: 52,
        goldImageClassifications: 6,
        correctGoldClassifications: 5,
        goldAccuracy: 83.3,
        credibilityScore: 0.81,
        currentStreak: 0,
        points: 260,
    },
];

export function getTaskAnalytics(taskId: number): TaskAnalytics {
    // Return mock data for any task ID
    return {
        ...MOCK_TASK_ANALYTICS,
        taskId,
    };
}

export function getUserPerformance(taskId?: number): UserPerformance[] {
    // Return mock user performance data
    // If taskId is provided, we could filter by task in the future
    return MOCK_USER_PERFORMANCE;
}

export const analyticsMock = {
    taskAnalytics: MOCK_TASK_ANALYTICS,
    userPerformance: MOCK_USER_PERFORMANCE,
    getTaskAnalytics,
    getUserPerformance,
};
