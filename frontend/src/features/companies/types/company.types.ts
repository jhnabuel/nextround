export interface CompanyResponse{
    id: string;
    companyName: string;
    websiteUrl?: string | null;
    industry?: string | null;
    location?: string | null;
    createdAt: string;
    updatedAt: string;
}

export interface CompanyRequest {
    companyName: string;
    websiteUrl?: string | null;
    industry?: string | null;
    location?: string | null;
}

export type CreateCompanyRequest = CompanyRequest;
export type EditCompanyRequest = CompanyRequest;