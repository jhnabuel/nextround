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