import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { isAxiosError } from "axios";
import { Loader2, X } from "lucide-react";
import {
    companySchema,
    toCompanyPayload,
    type CompanyFormValues,
} from "../schemas/companySchema";
import { useCreateCompany, useUpdateCompany } from "../hooks/useCompanies";
import type { CompanyResponse } from "../types/company.types";

interface CompanyFormDialogProps {
    open: boolean;
    onClose: () => void;
    company?: CompanyResponse | null;
}

interface BackendErrorResponse {
    message?: string;
    errors?: Record<string, string>;
}

const VALID_FIELDS = new Set<keyof CompanyFormValues>([
    "companyName",
    "websiteUrl",
    "industry",
    "location",
]);

export function CompanyFormDialog({ open, onClose, company }: CompanyFormDialogProps) {
    const isEditMode = Boolean(company);
    const createCompany = useCreateCompany();
    const updateCompany = useUpdateCompany();
    const isPending = createCompany.isPending || updateCompany.isPending;

    const {
        register,
        handleSubmit,
        setError,
        formState: { errors },
    } = useForm<CompanyFormValues>({
        resolver: zodResolver(companySchema),
        defaultValues: {
            companyName: company?.companyName ?? "",
            websiteUrl: company?.websiteUrl ?? "",
            industry: company?.industry ?? "",
            location: company?.location ?? "",
        },
    });

    useEffect(() => {
        if (!open) return;

        const handleKeyDown = (event: KeyboardEvent) => {
            if (event.key === "Escape" && !isPending) {
                onClose();
            }
        };

        window.addEventListener("keydown", handleKeyDown);
        return () => window.removeEventListener("keydown", handleKeyDown);
    }, [open, isPending, onClose]);

    if (!open) return null;

    const onSubmit = async (values: CompanyFormValues) => {
        const payload = toCompanyPayload(values);

        try {
            if (isEditMode && company) {
                await updateCompany.mutateAsync({ id: company.id, payload });
            } else {
                await createCompany.mutateAsync(payload);
            }
            onClose();
        } catch (err) {
            if (!isAxiosError<BackendErrorResponse>(err)) return;

            const status = err.response?.status;
            const data = err.response?.data;

            if (status === 409) {
                setError("companyName", {
                    type: "server",
                    message: data?.message || "A company with this name already exists.",
                });
                return;
            }

            if (status === 400 && data?.errors) {
                Object.entries(data.errors).forEach(([field, message]) => {
                    if (VALID_FIELDS.has(field as keyof CompanyFormValues)) {
                        setError(field as keyof CompanyFormValues, {
                            type: "server",
                            message,
                        });
                    }
                });
            }
        }
    };

    return (
        <div
            className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 p-4 backdrop-blur-[1px]"
            onClick={() => {
                if (!isPending) onClose();
            }}
            role="dialog"
            aria-modal="true"
            aria-labelledby="company-dialog-title"
        >
            <div
                className="w-full max-w-md rounded-xl border border-slate-200 bg-white p-6 shadow-xl"
                onClick={(e) => e.stopPropagation()}
            >
                <div className="flex items-start justify-between gap-4">
                    <div>
                        <h2 id="company-dialog-title" className="text-lg font-semibold text-slate-900">
                            {isEditMode ? "Edit Company" : "Add Company"}
                        </h2>
                        <p className="mt-1 text-sm text-slate-600">
                            {isEditMode
                                ? "Update company details across your job applications."
                                : "Create a new company record to link with your applications."}
                        </p>
                    </div>

                    <button
                        type="button"
                        onClick={onClose}
                        disabled={isPending}
                        aria-label="Close dialog"
                        className="rounded-md p-1 text-slate-400 transition-colors hover:bg-slate-100 hover:text-slate-700 disabled:opacity-50"
                    >
                        <X className="h-4 w-4" />
                    </button>
                </div>

                <form onSubmit={handleSubmit(onSubmit)} className="mt-5 space-y-4" noValidate>
                    {/* Company Name */}
                    <div>
                        <label
                            htmlFor="companyName"
                            className="block text-sm font-medium text-slate-700"
                        >
                            Company Name <span className="text-red-600">*</span>
                        </label>
                        <input
                            id="companyName"
                            type="text"
                            autoFocus
                            disabled={isPending}
                            placeholder="e.g., Stripe"
                            {...register("companyName")}
                            className={`mt-1 w-full rounded-lg border px-3 py-2 text-sm text-slate-900 placeholder:text-slate-400 focus:outline-none focus:ring-1 disabled:bg-slate-50 ${
                                errors.companyName
                                    ? "border-red-500 focus:border-red-500 focus:ring-red-500"
                                    : "border-slate-300 focus:border-slate-900 focus:ring-slate-900"
                            }`}
                        />
                        {errors.companyName && (
                            <p className="mt-1 text-xs text-red-600">{errors.companyName.message}</p>
                        )}
                    </div>

                    {/* Website URL */}
                    <div>
                        <label
                            htmlFor="websiteUrl"
                            className="block text-sm font-medium text-slate-700"
                        >
                            Website URL
                        </label>
                        <input
                            id="websiteUrl"
                            type="url"
                            disabled={isPending}
                            placeholder="https://stripe.com"
                            {...register("websiteUrl")}
                            className={`mt-1 w-full rounded-lg border px-3 py-2 text-sm text-slate-900 placeholder:text-slate-400 focus:outline-none focus:ring-1 disabled:bg-slate-50 ${
                                errors.websiteUrl
                                    ? "border-red-500 focus:border-red-500 focus:ring-red-500"
                                    : "border-slate-300 focus:border-slate-900 focus:ring-slate-900"
                            }`}
                        />
                        {errors.websiteUrl && (
                            <p className="mt-1 text-xs text-red-600">{errors.websiteUrl.message}</p>
                        )}
                    </div>

                    {/* Industry */}
                    <div>
                        <label
                            htmlFor="industry"
                            className="block text-sm font-medium text-slate-700"
                        >
                            Industry
                        </label>
                        <input
                            id="industry"
                            type="text"
                            disabled={isPending}
                            placeholder="e.g., Financial Technology"
                            {...register("industry")}
                            className={`mt-1 w-full rounded-lg border px-3 py-2 text-sm text-slate-900 placeholder:text-slate-400 focus:outline-none focus:ring-1 disabled:bg-slate-50 ${
                                errors.industry
                                    ? "border-red-500 focus:border-red-500 focus:ring-red-500"
                                    : "border-slate-300 focus:border-slate-900 focus:ring-slate-900"
                            }`}
                        />
                        {errors.industry && (
                            <p className="mt-1 text-xs text-red-600">{errors.industry.message}</p>
                        )}
                    </div>

                    {/* Location */}
                    <div>
                        <label
                            htmlFor="location"
                            className="block text-sm font-medium text-slate-700"
                        >
                            Location
                        </label>
                        <input
                            id="location"
                            type="text"
                            disabled={isPending}
                            placeholder="e.g., San Francisco, CA / Remote"
                            {...register("location")}
                            className={`mt-1 w-full rounded-lg border px-3 py-2 text-sm text-slate-900 placeholder:text-slate-400 focus:outline-none focus:ring-1 disabled:bg-slate-50 ${
                                errors.location
                                    ? "border-red-500 focus:border-red-500 focus:ring-red-500"
                                    : "border-slate-300 focus:border-slate-900 focus:ring-slate-900"
                            }`}
                        />
                        {errors.location && (
                            <p className="mt-1 text-xs text-red-600">{errors.location.message}</p>
                        )}
                    </div>

                    {/* Actions */}
                    <div className="flex items-center justify-end gap-2 pt-2">
                        <button
                            type="button"
                            onClick={onClose}
                            disabled={isPending}
                            className="rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm font-medium text-slate-700 transition-colors hover:bg-slate-50 disabled:opacity-50"
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            disabled={isPending}
                            className="inline-flex items-center gap-2 rounded-lg bg-slate-900 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-slate-800 disabled:opacity-50"
                        >
                            {isPending && <Loader2 className="h-4 w-4 animate-spin" />}
                            <span>{isEditMode ? "Save Changes" : "Create Company"}</span>
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}