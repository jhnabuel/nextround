import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { isAxiosError } from "axios";
import { toast } from "sonner";
import { applicationService } from "../../../services/application.service";

import type {
    JobApplicationResponse,
    CreateJobApplicationRequest,
    UpdateJobApplicationRequest,
} from "../types/application.types";

export function useApplications(){
    return useQuery({
        queryKey: applicationKeys.lists(),
        queryFn: applicationService.getAll,
        staleTime: 1000 * 60 * 5,
    });
}

export function useApplication(id: string){
    return useQuery({
        queryKey: applicationKeys.detail(id),
        queryFn: () => applicationService.getById(id),
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
        }: {id: string; payload: JobApplicationRequest})
    })
}