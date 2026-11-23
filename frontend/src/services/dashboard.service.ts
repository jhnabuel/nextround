import { apiClient } from "../lib/apiClient";
import type { DashboardStatsResponse, DashboardActivityResponse, ActivityGranularity, } from "../features/dashboard/types/dashboard.types"

export const dashboardService = {
    getStats: async (): Promise<DashboardStatsResponse> => {
        const { data } = await apiClient.get<DashboardStatsResponse>("/api/v1/dashboard/stats");
        return data;
    },

    getActivity: async (
        granularity: ActivityGranularity = "WEEK"
    ): Promise<DashboardActivityResponse> => {
        const { data } = await apiClient.get<DashboardActivityResponse>("/api/v1/dashboard/activity", {
            params: { granularity },
        });
        return data;
    },
}