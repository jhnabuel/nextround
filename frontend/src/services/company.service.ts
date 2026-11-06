import { apiClient } from "../lib/apiClient";
import type { CompanySummary } from "../features/applications/types/application.types";
import type { SpringPage } from "../types/pagination.types";

export const companyService = {
    getAll: async (): Promise<CompanySummary[]> => {
        const response = await apiClient.get<SpringPage<CompanySummary> | CompanySummary[]>("/companies", {
            params: {
                size: 100, // Fetch up to 100 to populate the dropdown without truncation
            },
        });

        const payload = response.data as any;

        // Handle Spring Data Page<T>
        if (Array.isArray(payload?.content)) {
            return payload.content;
        }

        // Handle generic wrapper ({ data: [...] })
        if (Array.isArray(payload?.data)) {
            return payload.data;
        }

        // Handle direct list/array
        if (Array.isArray(payload)) {
            return payload;
        }

        return [];
    },
};