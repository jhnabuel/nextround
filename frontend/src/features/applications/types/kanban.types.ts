import type { ApplicationStatus, WorkLocationType, JobApplicationResponse } from "./application.types";

export interface KanbanApplication {
    id: string;
    jobTitle: string;
    companyName: string;
    location?: string;
    workLocation: WorkLocationType;
    salaryMin?: number;
    salaryMax?: number;
    salaryCurrency?: string;
    appliedDate?: string;
    createdAt?:string;
    status: ApplicationStatus;
}

export interface ColumnConfig {
    id: ApplicationStatus;
    title: string;
    badgeBg: string;
    badgeText: string;
    dotColor: string;
}

export const KANBAN_COLUMNS: ColumnConfig[] = [
    {
        id: "WISHLIST",
        title: "Wishlist",
        badgeBg: "bg-[#F1F5F9]",
        badgeText: "text-[#475569]",
        dotColor: "bg-[#64748B]",
    },
    {
        id: "APPLIED",
        title: "Applied",
        badgeBg: "bg-[#DBEAFE]",
        badgeText: "text-[#1D4ED8]",
        dotColor: "bg-[#2563EB]",
    },
    {
        id: "INTERVIEWING",
        title: "Interviewing",
        badgeBg: "bg-[#FEF3C7]",
        badgeText: "text-[#92400E]",
        dotColor: "bg-[#D97706]",
    },
    {
        id: "OFFER_RECEIVED",
        title: "Offer Received",
        badgeBg: "bg-[#DCFCE7]",
        badgeText: "text-[#166534]",
        dotColor: "bg-[#16A34A]",
    },
    {
        id: "ACCEPTED",
        title: "Accepted",
        badgeBg: "bg-[#D1FAE5]",
        badgeText: "text-[#065F46]",
        dotColor: "bg-[#059669]",
    },
    {
        id: "REJECTED",
        title: "Rejected",
        badgeBg: "bg-[#FEE2E2]",
        badgeText: "text-[#991B1B]",
        dotColor: "bg-[#DC2626]",
    },
    {
        id: "WITHDRAWN",
        title: "Withdrawn",
        badgeBg: "bg-[#F1F5F9]",
        badgeText: "text-[#64748B]",
        dotColor: "bg-[#94A3B8]",
    },
    {
        id: "GHOSTED",
        title: "Ghosted",
        badgeBg: "bg-[#F3E8FF]",
        badgeText: "text-[#6B21A8]",
        dotColor: "bg-[#9333EA]",
    },
];