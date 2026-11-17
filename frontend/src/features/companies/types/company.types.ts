export interface CompanyResponse{
    id: string;
    name: string;
    websiteUrl?: string;
    industry?: string;
    location?: string;
    createdAt: string;
    updatedAt: string;
}

export interface CompanyRequest {
    companyName: string;
    websiteUrl?: string;
    industry?: string;
    location?: string;
}

export type CreateCompanyRequest = CompanyRequest;
export type EditCompanyRequest = CompanyRequest;