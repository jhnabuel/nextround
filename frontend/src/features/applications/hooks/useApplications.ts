import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { isAxiosError } from "axios";
import { toast } from "sonner";
import { applicationService, type ApplicationQueryParams } from "../../../services/application.service";
import { applicationKeys } from "../api/applicationKeys";

import type {
    JobApplicationResponse,
    CreateJobApplicationRequest,
    UpdateJobApplicationRequest,
} from "../types/application.types";

export function useApplications(params?: ApplicationQueryParams){
    return useQuery({
        queryKey: params ? applicationKeys.list(params as Record<string, unknown>) : applicationKeys.lists(),
        queryFn: () => applicationService.getAll(params),
        staleTime: 1000 * 60 * 5,
    });
}

export function useApplication(id: string){
    return useQuery({
        queryKey: applicationKeys.detail(id),
        queryFn: () => applicationService.getById(id),
        // only fetch if userId exists
        enabled: Boolean(id),
    });
}


export function useCreateApplication(){
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (payload: CreateJobApplicationRequest) => applicationService.create(payload),
        onSuccess: (newApp) => {
            queryClient.invalidateQueries({ queryKey: applicationKeys.lists() });
            toast.success(`Added application for ${newApp.jobTitle} at ${newApp.company.name}`);
        },
        onError: (err: unknown) => {
            let message = "Failed to create application.";
            if(isAxiosError<{ message?: string}> (err) && err.response?.data?.message){
                message = err.response.data.message;
            }
            toast.error(message);
        },
    });
}

export function useUpdateApplication(){
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: ({
            id, payload,
        }: {id: string;
            payload: UpdateJobApplicationRequest;
        }) => applicationService.update(id, payload),
        onSuccess: (updatedApp) => {
            // Direct cache write to detail query
            queryClient.setQueryData(
                applicationKeys.detail(updatedApp.id), updatedApp
            );
            // Invalidate lists to keep table/board in sync
            queryClient.invalidateQueries({ queryKey: applicationKeys.lists() });
            toast.success("Application updated successfully.");
        },
        onError: (err: unknown) => {
            let message = "Failed to update application.";
            if (isAxiosError<{ message?: string }>(err) && err.response?.data?.message) {
                message = err.response.data.message;
            }
            toast.error(message);
        },
    });
}

export function useDeleteApplication(){
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (id: string) => applicationService.delete(id),

        onMutate: async (deletedId: string) => {
            await queryClient.cancelQueries({ queryKey: applicationKeys.lists() });

            const previousApps = queryClient.getQueryData<JobApplicationResponse[]>(
                applicationKeys.lists()
            );

            if (previousApps){
                queryClient.setQueryData<JobApplicationResponse[]>(
                    applicationKeys.lists(),
                    previousApps.filter((app) => app.id !== deletedId)
                );
            }

            return { previousApps };
        },

        onError: (_err, _id, context) => {
            // Rollback on failure
            if (context?.previousApps) {
                queryClient.setQueryData(applicationKeys.lists(), context.previousApps);
            }
            toast.error("Failed to delete application.");
        },
        onSuccess: () => {
            toast.success("Application deleted.");
        },
        onSettled: () => {
            queryClient.invalidateQueries({ queryKey: applicationKeys.lists() });
        },
    });
}