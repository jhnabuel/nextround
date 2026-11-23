import type { ActivityGranularity } from "../types/dashboard.types";

export const dashboardKeys = {
    all: ["dashboard"] as const,
    stats: () => [...dashboardKeys.all, "stats"] as const,
    activities: () => [...dashboardKeys.all, "activity"] as const,
    activity: (granularity: ActivityGranularity) =>
        [...dashboardKeys.activities(), { granularity }] as const,
};