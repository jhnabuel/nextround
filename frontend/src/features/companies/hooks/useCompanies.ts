import { useQuery, useMutation, useQueryClient, keepPreviousData } from "@tanstack/react-query";
import { isAxiosError } from "axios";
import { toast } from "sonner";
import { companyService } from "../../../services/company.service";
import { companyKeys } from "../api/companyKeys";
import { applicationKeys } from "../../applications/api/applicationKeys";
import type { CreateCompanyRequest, EditCompanyRequest } from "../types/company.types";


interface UpdateCompanyVariables {
    id: string;
    payload: EditCompanyRequest;
}

interface ApiErrorPayload {
    message?: string;
}

export function useCompanies(search?: string){
    return useQuery({
       queryKey: companyKeys.list(search),
        queryFn: () => companyService.getAll(search),
        placeholderData: keepPreviousData,
        staleTime: 1000 * 60 * 2,
    });
}

export function useCreateCompany(){
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (payload: CreateCompanyRequest) => companyService.create(payload),
        onSuccess: (newCompany) => {
            queryClient.invalidateQueries({ queryKey: companyKeys.lists() });
            toast.success(`Added ${newCompany.companyName}.`, { duration: 4000 });
        },
        onError: (err:unknown) => {
            if (isAxiosError<ApiErrorPayload>(err)) {
                if (err.response?.status === 409) {
                    toast.error(err.response.data?.message || "A company with this name already exists.");
                    return;
                }
                toast.error(err.response?.data?.message || "Failed to create company.");
                return;
            }
            toast.error("Failed to create company.");
        },
    });
}

export function useUpdateCompany(){
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: ({id, payload}: UpdateCompanyVariables) => companyService.update(id, payload),
        onSuccess: (updatedCompany) => {
            queryClient.setQueryData(companyKeys.detail(updatedCompany.id), updatedCompany);
            queryClient.invalidateQueries({ queryKey: companyKeys.lists() });
            // Sync renamed companies across Kanban application cards
            queryClient.invalidateQueries({ queryKey: applicationKeys.lists() });
            toast.success(`Updated ${updatedCompany.companyName}.`, { duration: 4000 });
        },
        onError: (err: unknown) => {
            if (isAxiosError<ApiErrorPayload>(err)){
                if (err.response?.status === 409){
                    toast.error(err.response.data?.message || "A company with this name already exists.");
                    return;
                }
                toast.error(err.response?.data?.message || "Failed to update company.");
                return;
            }
            toast.error("Failed to update company.");
        },
    });
}

export function useDeleteCompany() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (id: string) => companyService.delete(id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: companyKeys.lists() });
            toast.success("Company deleted.", { duration: 4000 });
        },
        onError: (err: unknown) => {
            // 409 Conflict is displayed inline inside DeleteCompanyDialog
            if (isAxiosError(err) && err.response?.status === 409) {
                return;
            }
            const message = isAxiosError<ApiErrorPayload>(err)
                ? err.response?.data?.message || "Failed to delete company."
                : "Failed to delete company.";
            toast.error(message);
        },
    });
}