import { apiClient } from "./apiClient";
import type {
    JobApplicationResponse,
    CreateApplicationRequest,
    UpdateApplicationRequest,
} from "../features/applications/types/application.types";

export type Application = JobApplicationResponse;

export const applicationService = {
    getAll: async (): Promise<JobApplicationResponse[]> =>{
        const response = apiClient.get<JobApplicationResponse[]>("/applications");
        return response.data;
    },

    getById: async (id: string): Promise<JobApplicationResponse> => {
        const response = await apiClient.get<JobApplicationResponse>(`/applications/${id}`);
        return response.data;
    },

    create: async (payload: CreateApplicationRequest): Promise<JobApplicationResponse> => {
        const response = await apiClient.post<JobApplicationResponse>("/applications", payload);
        return response.data;
    },

    update: async (id: string, payload: UpdateApplicationRequest): Promise<JobApplicationResponse> => {
        const response = await apiClient.put<JobApplicationRequest>(`/applications/${id}`, payload);
        return response.data;
    },

    delete: async (id: string): Promise<JobApplicationResponse> => {
       await apiClient.delete(`/applications/${id}`);
    },
};