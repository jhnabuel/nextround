import { z } from "zod";
import type { CompanyRequest } from "../types/company.types";

export const companySchema = z.object({
    companyName: z
        .string()
        .trim()
        .min(1, "Company name is required")
        .max(100, "Company name cannot exceed 100 characters"),
    websiteUrl: z
        .string()
        .trim()
        .url("Invalid website URL format (include http:// or https://)")
        .optional()
        .or(z.literal("")),
    industry: z
        .string()
        .trim()
        .max(100, "Industry cannot exceed 100 characters")
        .optional()
        .or(z.literal("")),
    location: z
        .string()
        .trim()
        .max(100, "Location cannot exceed 100 characters")
        .optional()
        .or(z.literal("")),
});

export type CompanyFormValues = z.infer<typeof companySchema>;

export function toCompanyPayload(values: CompanyFormValues): CompanyRequest {
    return {
        companyName: values.companyName.trim(),
        websiteUrl: values.websiteUrl?.trim() || null,
        industry: values.industry?.trim() || null,
        location: values.location?.trim() || null,
    };
}