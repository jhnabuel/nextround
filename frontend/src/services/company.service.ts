import { apiClient } from "../lib/apiClient";
import type { CompanySummary } from "../features/applications/types/application.types";
import type { SpringPage } from "../types/pagination.types";

export const companyService = {
    getAll: async (search?: string): Promise<CompanySummary[]> => {
        const response = await apiClient.get<SpringPage<CompanySummary>>("/companies", {
            params: {
                search: search?.trim() || undefined,
                size: 15,
                sort: "companyName,asc",
            },
        });

        return response.data?.content ?? [];
    },
};