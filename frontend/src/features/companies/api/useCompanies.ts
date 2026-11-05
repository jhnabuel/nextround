import { useQuery } from "@tanstack/react-query";
import { companyService } from "../../../services/company.service";

export const companyKeys = {
    all: ["companies"] as const,
    lists: () => [...companyKeys.all, "list"] as const,
};

export function useCompanies() {
    return useQuery({
        queryKey: companyKeys.lists(),
        queryFn: companyService.getAll,
        staleTime: 1000 * 60 * 10, // 10 minutes cache
    });
}