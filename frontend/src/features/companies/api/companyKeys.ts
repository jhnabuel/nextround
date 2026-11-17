export const companyKeys = {
    all: ["companies"] as const,
    lists: () => [...companyKeys.all, "list"] as const,
    list: (search?: string) => [...companyKeys.lists(), search?.trim() ?? ""] as const,
    details: () => [...companyKeys.all, "detail"] as const,
    detail: (id: string) => [...companyKeys.details(), id] as const,
};