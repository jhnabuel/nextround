import { useEffect } from "react";
import { useForm, Controller } from "react-hook-form";
import { X, Loader2 } from "lucide-react";
import { CompanyCombobox } from "../../companies/components/CompanyCombobox";
import {
    useCreateApplication,
    useUpdateApplication,
} from "../hooks/useApplications";

import type {
    JobApplicationResponse,
    CreateJobApplicationRequest,
    ApplicationStatus,
    WorkLocationType,
} from "../types/application.types";

interface ApplicationFormDialogProps {
    isOpen: boolean;
    onClose: () => void;
    initialData?: JobApplicationResponse | null;
}

type FormValues = {
    jobTitle: string;
    companyId?: string;
    companyName?: string;
    jobUrl?: string;
    applicationStatus: ApplicationStatus;
    workLocation: WorkLocationType;
    salaryMin?: number | string;
    salaryMax?: number | string;
    currency?: string;
    appliedDate: string;
};

const STATUS_OPTIONS: { label: string; value: ApplicationStatus }[] = [
    { label: "Wishlist", value: "WISHLIST" },
    { label: "Applied", value: "APPLIED" },
    { label: "Interviewing", value: "INTERVIEWING" },
    { label: "Offer Received", value: "OFFER_RECEIVED" },
    { label: "Accepted", value: "ACCEPTED" },
    { label: "Rejected", value: "REJECTED" },
    { label: "Withdrawn", value: "WITHDRAWN" },
    { label: "Ghosted", value: "GHOSTED" },
];

const LOCATION_OPTIONS: { label: string; value: WorkLocationType }[] = [
    { label: "Remote", value: "REMOTE" },
    { label: "Hybrid", value: "HYBRID" },
    { label: "On-site", value: "ON_SITE" },
];

export function ApplicationFormDialog({
                                          isOpen,
                                          onClose,
                                          initialData,
                                      }: ApplicationFormDialogProps) {
    const createMutation = useCreateApplication();
    const updateMutation = useUpdateApplication();

    const isEditing = Boolean(initialData);
    const isPending = createMutation.isPending || updateMutation.isPending;

    const {
        register,
        handleSubmit,
        control,
        reset,
        formState: { errors },
    } = useForm<FormValues>({
        defaultValues: {
            jobTitle: "",
            companyId: undefined,
            companyName: "",
            jobUrl: "",
            applicationStatus: "APPLIED",
            workLocation: "REMOTE",
            salaryMin: "",
            salaryMax: "",
            currency: "PHP",
            appliedDate: new Date().toISOString().split("T")[0],
        },
    });

    useEffect(() => {
        if (initialData) {
            reset({
                jobTitle: initialData.jobTitle,
                companyId: initialData.company.id,
                companyName: initialData.company.companyName,
                jobUrl: initialData.jobUrl || "",
                applicationStatus: initialData.applicationStatus,
                workLocation: initialData.workLocation,
                salaryMin: initialData.salaryMin ?? "",
                salaryMax: initialData.salaryMax ?? "",
                currency: initialData.currency || "PHP",
                appliedDate: initialData.appliedDate?.split("T")[0] || "",
            });
        } else {
            reset({
                jobTitle: "",
                companyId: undefined,
                companyName: "",
                jobUrl: "",
                applicationStatus: "APPLIED",
                workLocation: "REMOTE",
                salaryMin: "",
                salaryMax: "",
                currency: "PHP",
                appliedDate: new Date().toISOString().split("T")[0],
            });
        }
    }, [initialData, reset, isOpen]);

    if (!isOpen) return null;

    const onSubmit = async (values: FormValues) => {
        const payload: CreateJobApplicationRequest = {
            jobTitle: values.jobTitle.trim(),
            companyId: values.companyId || undefined,
            companyName: !values.companyId ? values.companyName?.trim() : undefined,
            jobUrl: values.jobUrl?.trim() || undefined,
            applicationStatus: values.applicationStatus,
            workLocation: values.workLocation,
            salaryMin: values.salaryMin ? Number(values.salaryMin) : undefined,
            salaryMax: values.salaryMax ? Number(values.salaryMax) : undefined,
            currency: values.currency || "PHP",
            appliedDate: values.appliedDate || undefined,
        };

        if (isEditing && initialData) {
            await updateMutation.mutateAsync({
                id: initialData.id,
                payload,
            });
        } else {
            await createMutation.mutateAsync(payload);
        }

        onClose();
    };

    const inputClass = (hasError: boolean) =>
        `h-10 w-full rounded-lg border bg-white px-3 text-xs text-[#2E3532]
    placeholder:text-[#94A3B8] outline-none transition-all
    ${
            hasError
                ? "border-rose-400 focus:ring-1 focus:ring-rose-200"
                : "border-[#E2E8F0] hover:border-[#CBD5E1] focus:border-[#7E9181] focus:ring-1 focus:ring-[#7E9181]"
        }`;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-xs p-4 animate-in fade-in-0 duration-150">
            <div className="w-full max-w-lg rounded-xl border border-[#E2E8F0] bg-white p-6 shadow-xl">
                <div className="flex items-center justify-between pb-4 border-b border-[#E2E8F0]">
                    <div>
                        <h2 className="text-base font-bold text-[#2E3532]">
                            {isEditing ? "Edit Application" : "New Application"}
                        </h2>
                        <p className="text-xs text-[#64748B] mt-0.5">
                            {isEditing ? "Update role parameters or status" : "Add a new opportunity to your pipeline"}
                        </p>
                    </div>
                    <button
                        type="button"
                        onClick={onClose}
                        className="rounded-md p-1 text-[#64748B] hover:bg-[#F1F5F9] transition-colors"
                    >
                        <X className="h-4 w-4" />
                    </button>
                </div>

                <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 pt-4">
                    {/* Job Title */}
                    <div>
                        <label className="block text-xs font-medium text-[#2E3532] mb-1">
                            Job Title <span className="text-rose-500">*</span>
                        </label>
                        <input
                            type="text"
                            placeholder="e.g. Software Engineer II"
                            {...register("jobTitle", { required: "Job title is required" })}
                            className={inputClass(Boolean(errors.jobTitle))}
                        />
                        {errors.jobTitle && (
                            <p className="mt-1 text-[11px] text-rose-600">{errors.jobTitle.message}</p>
                        )}
                    </div>

                    {/* Company Search Combobox */}
                    <div>
                        <label className="block text-xs font-medium text-[#2E3532] mb-1">
                            Company <span className="text-rose-500">*</span>
                        </label>
                        <Controller
                            name="companyName"
                            control={control}
                            rules={{
                                validate: (_, formValues) =>
                                    Boolean(formValues.companyId || formValues.companyName?.trim()) ||
                                    "Company name or selection is required",
                            }}
                            render={({ fieldState }) => (
                                <Controller
                                    name="companyId"
                                    control={control}
                                    render={({ field: idField }) => (
                                        <Controller
                                            name="companyName"
                                            control={control}
                                            render={({ field: nameField }) => (
                                                <CompanyCombobox
                                                    selectedCompanyId={idField.value}
                                                    selectedCompanyName={nameField.value}
                                                    onSelect={({ companyId, companyName }) => {
                                                        idField.onChange(companyId);
                                                        nameField.onChange(companyName);
                                                    }}
                                                    error={Boolean(fieldState.error)}
                                                />
                                            )}
                                        />
                                    )}
                                />
                            )}
                        />
                        {errors.companyName && (
                            <p className="mt-1 text-[11px] text-rose-600">{errors.companyName.message}</p>
                        )}
                    </div>

                    {/* Status & Work Location */}
                    <div className="grid grid-cols-2 gap-3">
                        <div>
                            <label className="block text-xs font-medium text-[#2E3532] mb-1">Status</label>
                            <select {...register("applicationStatus")} className={inputClass(false)}>
                                {STATUS_OPTIONS.map((opt) => (
                                    <option key={opt.value} value={opt.value}>
                                        {opt.label}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div>
                            <label className="block text-xs font-medium text-[#2E3532] mb-1">Work Location</label>
                            <select {...register("workLocation")} className={inputClass(false)}>
                                {LOCATION_OPTIONS.map((opt) => (
                                    <option key={opt.value} value={opt.value}>
                                        {opt.label}
                                    </option>
                                ))}
                            </select>
                        </div>
                    </div>

                    {/* Salary Ranges */}
                    <div className="grid grid-cols-3 gap-2">
                        <div>
                            <label className="block text-xs font-medium text-[#2E3532] mb-1">Min Salary</label>
                            <input
                                type="number"
                                placeholder="e.g. 70000"
                                {...register("salaryMin")}
                                className={inputClass(false)}
                            />
                        </div>
                        <div>
                            <label className="block text-xs font-medium text-[#2E3532] mb-1">Max Salary</label>
                            <input
                                type="number"
                                placeholder="e.g. 95000"
                                {...register("salaryMax")}
                                className={inputClass(false)}
                            />
                        </div>
                        <div>
                            <label className="block text-xs font-medium text-[#2E3532] mb-1">Currency</label>
                            <input
                                type="text"
                                placeholder="PHP"
                                {...register("currency")}
                                className={inputClass(false)}
                            />
                        </div>
                    </div>

                    {/* Job URL & Applied Date */}
                    <div className="grid grid-cols-2 gap-3">
                        <div>
                            <label className="block text-xs font-medium text-[#2E3532] mb-1">Job Posting URL</label>
                            <input
                                type="url"
                                placeholder="https://..."
                                {...register("jobUrl")}
                                className={inputClass(false)}
                            />
                        </div>
                        <div>
                            <label className="block text-xs font-medium text-[#2E3532] mb-1">Applied Date</label>
                            <input type="date" {...register("appliedDate")} className={inputClass(false)} />
                        </div>
                    </div>

                    {/* Actions */}
                    <div className="flex items-center justify-end gap-2 pt-4 border-t border-[#E2E8F0]">
                        <button
                            type="button"
                            onClick={onClose}
                            disabled={isPending}
                            className="rounded-lg border border-[#E2E8F0] px-4 py-2 text-xs font-medium text-[#2E3532] hover:bg-[#F8FAFC] transition-colors"
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            disabled={isPending}
                            className="flex items-center justify-center gap-1.5 rounded-lg bg-[#2E3532] px-4 py-2 text-xs font-semibold text-white transition-opacity hover:opacity-90 disabled:opacity-50"
                        >
                            {isPending && <Loader2 className="h-3.5 w-3.5 animate-spin text-[#7E9181]" />}
                            <span>{isEditing ? "Save Changes" : "Create Application"}</span>
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}