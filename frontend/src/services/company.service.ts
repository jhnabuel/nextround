import { apiClient } from "../lib/apiClient";
import type { SpringPage } from "../types/pagination.types";
import type {
    CompanyResponse,
    CreateCompanyRequest,
    EditCompanyRequest,
} from "../features/companies/types/company.types";


export const companyService = {
    getAll: async (search?: string): Promise<CompanyResponse[]> => {
        const response = await apiClient.get<SpringPage<CompanyResponse>>("/companies", {
            params: {
                search: search?.trim() || undefined,
                size: 100,
                sort: "companyName,asc",
            },
        });

        return response.data?.content ?? [];
    },

    async getById(id: string): Promise<CompanyResponse>{
        const response = await apiClient.get<CompanyResponse>(`/companies/${id}`);
        return response.data;
    },

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