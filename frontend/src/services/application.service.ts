import { apiClient } from "../lib/apiClient";
import type {
    JobApplicationResponse,
    CreateJobApplicationRequest,
    UpdateJobApplicationRequest,
    ApplicationStatus
} from "../features/applications/types/application.types";

import type { SpringPage } from "../types/pagination.types";

export type Application = JobApplicationResponse;

export interface ApplicationQueryParams {
    status?: ApplicationStatus;
    companyId?: string;
    page?: number;
    size?: number;
    sort?: string;
}

export const applicationService = {
    // GET /api/v1/applications -> Spring Data Page<T>
    getAll: async (params?: ApplicationQueryParams): Promise<JobApplicationResponse[]> => {
        const response = await apiClient.get<SpringPage<JobApplicationResponse>>("/applications", {
            params: {
                size: params?.size ?? 100, // Fetch up to 100 for the full Kanban board
                page: params?.page ?? 0,
                status: params?.status,
                companyId: params?.companyId,
                sort: params?.sort,
            },
        });

        // Extract the records array from the Spring Data page envelope
        return response.data?.content ?? [];
    },
    getById: async (id: string): Promise<JobApplicationResponse> => {
        const response = await apiClient.get<JobApplicationResponse>(`/applications/${id}`);
        return response.data;
    },

    create: async (payload: CreateJobApplicationRequest): Promise<JobApplicationResponse> => {
        const response = await apiClient.post<JobApplicationResponse>("/applications", payload);
        return response.data;
    },

    update: async (id: string, payload: UpdateJobApplicationRequest): Promise<JobApplicationResponse> => {
        const response = await apiClient.put<JobApplicationResponse>(`/applications/${id}`, payload);
        return response.data;
    },

    delete: async (id: string): Promise<void> => {
       await apiClient.delete(`/applications/${id}`);
    },
};