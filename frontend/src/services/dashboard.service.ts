import { apiClient } from "../lib/apiClient";
import type { DashboardStatsResponse, DashboardActivityResponse, ActivityGranularity, } from "../features/dashboard/types/dashboard.types"

export const dashboardService = {
    getStats: async (): Promise<DashboardStatsResponse> => {
        const { data } = await apiClient.get<DashboardStatsResponse>("/dashboard/stats");
        return data;
    },

    getActivity: async (
        granularity: ActivityGranularity = "WEEK"
    ): Promise<DashboardActivityResponse> => {
        const { data } = await apiClient.get<DashboardActivityResponse>("/dashboard/activity", {
            params: { granularity },
        });
        return data;
    },
}