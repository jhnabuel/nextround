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
    name: string;
    websiteUrl?: string;
    industry?: string;
    location?: string;
}

export interface JobApplicationResponse {
    id: string;
    jobTitle: string;
    jobUrl?: string;
    applicationStatus: ApplicationStatus;
    workLocation: WorkLocationType;
    salaryMin?: number;
    salaryMax?: number;
    salaryCurrency?: string;
    appliedDate?: string;
    company: CompanySummary;
    createdAt: string;
}

export interface CreateJobApplicationRequest {
    jobTitle: string;
    jobUrl?: string;
    companyId: string;
    applicationStatus: ApplicationStatus;
    workLocation: WorkLocationType;
    salaryMin?: number;
    salaryMax?: number;
    salaryCurrency?: string;
    appliedDate?: string;
}

export interface UpdateJobApplicationRequest {
    jobTitle?: string;
    jobUrl?: string;
    applicationStatus?: ApplicationStatus;
    workLocation?: WorkLocationType;
    salaryMin?: number;
    salaryMax?: number;
    salaryCurrency?: string;
    appliedDate?: string;
}