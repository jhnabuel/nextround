export interface DashboardStatsResponse{
    totalApplications: number;
    applied: number;
    interviewing: number;
    offers: number;
    rejected: number;
    withdrawn: number;
    ghosted: number;
}

export type ActivityGranularity = "WEEK" | "MONTH";

export interface ActivityPoint {
    period: string; // "YYYY-MM-DD" for WEEK, "YYYY-MM" for MONTH
    count: number;
}

export interface DashboardActivityResponse {
    granularity: ActivityGranularity;
    data: ActivityPoint[];
}