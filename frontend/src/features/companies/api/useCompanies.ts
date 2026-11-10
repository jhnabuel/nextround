import { useQuery } from "@tanstack/react-query";
import { companyService } from "../../../services/company.service";

export const companyKeys = {
    all: ["companies"] as const,
    lists: () => [...companyKeys.all, "list"] as const,
    list: (search?: string) => [...companyKeys.lists(), { search: search?.trim() || "" }] as const,
};

export function useCompanies(search?: string) {
    const query = search?.trim() || "";
    return useQuery({
        queryKey: companyKeys.list(query),
        queryFn: () => companyService.getAll(query),
        staleTime: 1000 * 60 * 2, // 2 mins cache
    });
}