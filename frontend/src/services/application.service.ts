import { apiClient } from "../lib/apiClient";
import type {
    JobApplicationResponse,
    CreateJobApplicationRequest,
    UpdateJobApplicationRequest,
} from "../features/applications/types/application.types";

export type Application = JobApplicationResponse;

export const applicationService = {
    getAll: async (): Promise<JobApplicationResponse[]> =>{
        const response = await apiClient.get<JobApplicationResponse[]>("/applications");
        return response.data;
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