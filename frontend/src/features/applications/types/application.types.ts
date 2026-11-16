export type ApplicationStatus =
    | "WISHLIST"
    | "APPLIED"
    | "INTERVIEWING"
    | "OFFER_RECEIVED"
    | "ACCEPTED"
    | "REJECTED"
    | "WITHDRAWN"
    | "GHOSTED";



export type WorkLocationType = "REMOTE" | "HYBRID" | "ON_SITE";

export interface CompanySummary {
    id: string;
    companyName: string;
    websiteUrl?: string;
    industry?: string;
    location?: string;
    createdAt?: string;
    updatedAt?: string;
}


export interface JobApplicationResponse {
    id: string;
    jobTitle: string;
    jobUrl?: string;
    applicationStatus: ApplicationStatus;
    workLocation: WorkLocationType;
    salaryMin?: number;
    salaryMax?: number;
    currency?: string;
    appliedDate: string;
    company: CompanySummary;
    createdAt: string;
    updatedAt: string;
}

export interface CreateJobApplicationRequest {
    jobTitle: string;
    jobUrl?: string;
    companyId?: string;
    companyName?: string;
    applicationStatus: ApplicationStatus;
    workLocation: WorkLocationType;
    salaryMin?: number;
    salaryMax?: number;
    currency?: string;
    appliedDate?: string;
}

export interface UpdateJobApplicationRequest {
    jobTitle?: string;
    jobUrl?: string;
    companyId?: string;
    companyName?: string;
    applicationStatus?: ApplicationStatus;
    workLocation?: WorkLocationType;
    salaryMin?: number;
    salaryMax?: number;
    currency?: string;
    appliedDate?: string;
}

