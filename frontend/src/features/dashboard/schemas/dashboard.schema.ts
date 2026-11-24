import { z } from "zod";

export const activityGranularitySchema = z.enum(["WEEK", "MONTH"], {
    error: "Granularity must be either WEEK or MONTH.",
});

export const dashboardFilterSchema = z.object({
    granularity: activityGranularitySchema.default("WEEK"),
});

export type DashboardFilterValues = z.infer<typeof dashboardFilterSchema>;