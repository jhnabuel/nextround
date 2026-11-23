import { keepPreviousData, useQuery } from "@tanstack/react-query";
import { dashboardService } from "../../../services/dashboard.service";
import { dashboardKeys } from "../api/dashboardKeys";
import type { ActivityGranularity } from "../types/dashboard.types";

export function useDashboardStats(){
    return useQuery({
        queryKey: dashboardKeys.stats(),
        queryFn: () => dashboardService.getStats(),
        staleTime: 1000 * 60 * 5,
    });
}

export function useDashboardActivity(granularity: ActivityGranularity = "WEEK") {
    return useQuery({
        queryKey: dashboardKeys.activity(granularity),
        queryFn: () => dashboardService.getActivity(granularity),
        staleTime: 1000 * 60 * 5,
        placeholderData: keepPreviousData,
    });
}