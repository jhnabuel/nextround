import { apiClient } from "./apiClient";
import type { CompanySummary } from "../features/applications/types/application.types";

export const companyService = {
    getAll: async (): Promise<CompanySummary[]> => {
        const response = await apiClient.get<CompanySummary[]>("/companies");
        return response.data;
    },
};