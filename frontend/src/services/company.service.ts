import { apiClient } from "../lib/apiClient";
import type { CompanySummary } from "../features/applications/types/application.types";
import type { SpringPage } from "../types/pagination.types";
import type {
    CompanyResponse,
    CreateCompanyRequest,
    EditCompanyRequest,
} from "../features/companies/types/company.types";


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

    async getById(id: string): Promise<CompanyResponse>{
        const response = await apiClient.get<CompanyResponse>(`/companies/${id}`);
    }

    async create(payload: CreateCompanyRequest): Promise<CompanyResponse> {
        const response = await apiClient.post<CompanyResponse>("/companies", payload);
        return response.data;
    },

    async update(id: string, payload: EditCompanyRequest): Promise<CompanyResponse> {
        const response = await apiClient.patch<CompanyResponse>(`/companies/${id}`, payload);
        return response.data;
    },

    async delete(id: string): Promise<void> {
        await apiClient.delete(`/companies/${id}`);
    },
};