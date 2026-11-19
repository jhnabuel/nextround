import { useEffect, useState } from "react";
import { isAxiosError } from "axios";
import { AlertTriangle, Loader2, X } from "lucide-react";
import { useDeleteCompany } from "../hooks/useCompanies";
import type { CompanyResponse } from "../types/company.types";

interface DeleteCompanyDialogProps {
    open: boolean;
    company: CompanyResponse | null;
    onClose: () => void;
}

export function DeleteCompanyDialog({
                                        open,
                                        company,
                                        onClose,
                                    }: DeleteCompanyDialogProps) {
    const deleteCompany = useDeleteCompany();
    const [conflictMessage, setConflictMessage] = useState<string | null>(null);
    const isPending = deleteCompany.isPending;

    useEffect(() => {
        setConflictMessage(null);
    }, [company?.id, open]);

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

    if (!open || !company) return null;

    const handleConfirmDelete = async () => {
        setConflictMessage(null);

        try {
            await deleteCompany.mutateAsync(company.id);
            onClose();
        } catch (err) {
            if (isAxiosError(err) && err.response?.status === 409) {
                setConflictMessage(
                    `Cannot delete ${company.companyName} because it still has linked job applications. Reassign or remove those applications first.`
                );
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
            aria-labelledby="delete-company-title"
        >
            <div
                className="w-full max-w-md rounded-xl border border-slate-200 bg-white p-6 shadow-xl"
                onClick={(e) => e.stopPropagation()}
            >
                <div className="flex items-start justify-between gap-4">
                    <div className="flex items-start gap-3">
                        <div className="rounded-full bg-red-100 p-2 text-red-600">
                            <AlertTriangle className="h-5 w-5" />
                        </div>
                        <div>
                            <h2
                                id="delete-company-title"
                                className="text-lg font-semibold text-slate-900"
                            >
                                Delete Company
                            </h2>
                            <p className="mt-1 text-sm text-slate-600">
                                Are you sure you want to delete{" "}
                                <span className="font-semibold text-slate-900">
                  {company.companyName}
                </span>
                                ? This action cannot be undone.
                            </p>
                        </div>
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

                {conflictMessage && (
                    <div
                        role="alert"
                        className="mt-4 rounded-lg border border-red-200 bg-red-50 p-3 text-sm text-red-800"
                    >
                        {conflictMessage}
                    </div>
                )}

                <div className="mt-6 flex items-center justify-end gap-2">
                    <button
                        type="button"
                        onClick={onClose}
                        disabled={isPending}
                        className="rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm font-medium text-slate-700 transition-colors hover:bg-slate-50 disabled:opacity-50"
                    >
                        {conflictMessage ? "Close" : "Cancel"}
                    </button>

                    {!conflictMessage && (
                        <button
                            type="button"
                            onClick={handleConfirmDelete}
                            disabled={isPending}
                            className="inline-flex items-center gap-2 rounded-lg bg-red-600 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-red-700 disabled:opacity-50"
                        >
                            {isPending && <Loader2 className="h-4 w-4 animate-spin" />}
                            <span>Delete Company</span>
                        </button>
                    )}
                </div>
            </div>
        </div>
    );
}