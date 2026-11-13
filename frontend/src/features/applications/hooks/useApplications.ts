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

interface UpdateApplicationVariables {
    id: string;
    payload: UpdateJobApplicationRequest;
    successMessage?: string;
}

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

            const companyDisplayName =
                newApp.company?.companyName ||
                (newApp.company as any)?.name ||
                "company";

            toast.success(
                `Added application for ${newApp.jobTitle} at ${companyDisplayName}`,
                { duration: 4000 }
            );
        },
        onError: (err: unknown) => {
            let message = "Failed to create application.";
            if(isAxiosError<{ message?: string}> (err) && err.response?.data?.message){
                message = err.response.data.message;
            }
            toast.error(message, {duration: 4000});
        },
    });
}
export function useUpdateApplication() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: ({ id, payload }: UpdateApplicationVariables) =>
            applicationService.update(id, payload),

        onMutate: async ({ id, payload }) => {
            await queryClient.cancelQueries({ queryKey: applicationKeys.lists() });

            // Snapshot all cached list queries for rollback
            const previousLists = queryClient.getQueriesData<JobApplicationResponse[]>({
                queryKey: applicationKeys.lists(),
            });

            // Optimistically update matching application across all list caches
            queryClient.setQueriesData<JobApplicationResponse[]>(
                { queryKey: applicationKeys.lists() },
                (old) =>
                    old?.map((app) =>
                        app.id === id
                            ? {
                                ...app,
                                ...payload,
                                applicationStatus: payload.applicationStatus ?? app.applicationStatus,
                                updatedAt: new Date().toISOString(),
                            }
                            : app
                    )
            );

            return { previousLists };
        },

        onError: (err: unknown, _variables, context) => {
            // Restore previous cache snapshots on failure
            context?.previousLists.forEach(([queryKey, data]) => {
                queryClient.setQueryData(queryKey, data);
            });

            let message = "Failed to update application.";
            if (isAxiosError<{ message?: string }>(err) && err.response?.data?.message) {
                message = err.response.data.message;
            }
            toast.error(message);
        },

        onSuccess: (updatedApp, variables) => {
            queryClient.setQueryData(applicationKeys.detail(updatedApp.id), updatedApp);
            toast.success(variables.successMessage ?? "Application updated successfully.", {
                duration: 4000,
            });
        },

        onSettled: (_data, _error, variables) => {
            queryClient.invalidateQueries({ queryKey: applicationKeys.lists() });
            queryClient.invalidateQueries({ queryKey: applicationKeys.detail(variables.id) });
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
            toast.error("Failed to delete application.", {duration: 4000});
        },
        onSuccess: () => {
            toast.success("Application deleted.", {duration: 4000});
        },
        onSettled: () => {
            queryClient.invalidateQueries({ queryKey: applicationKeys.lists() });
        },
    });
}