import { AlertTriangle, Loader2 } from "lucide-react";

interface ConfirmDialogProps {
    isOpen: boolean;
    title: string;
    description: string;
    confirmText?: string;
    cancelText?: string;
    variant?: "danger" | "default";
    isLoading?: boolean;
    onConfirm: () => void;
    onClose: () => void;
}

export function ConfirmDialog({
                                  isOpen,
                                  title,
                                  description,
                                  confirmText = "Confirm",
                                  cancelText = "Cancel",
                                  variant = "danger",
                                  isLoading = false,
                                  onConfirm,
                                  onClose,
                              }: ConfirmDialogProps) {
    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-xs p-4 animate-in fade-in-0 duration-150">
            <div
                role="alertdialog"
                aria-modal="true"
                aria-labelledby="confirm-dialog-title"
                aria-describedby="confirm-dialog-desc"
                className="w-full max-w-md rounded-xl border border-[#E2E8F0] bg-white p-6 shadow-xl animate-in zoom-in-95 duration-150"
            >
                <div className="flex items-start gap-3.5">
                    {variant === "danger" && (
                        <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-rose-50 text-rose-600 border border-rose-100">
                            <AlertTriangle className="h-5 w-5" />
                        </div>
                    )}

                    <div className="min-w-0 flex-1 pt-0.5">
                        <h3 id="confirm-dialog-title" className="text-sm font-semibold text-[#1E293B]">
                            {title}
                        </h3>
                        <p id="confirm-dialog-desc" className="mt-1.5 text-xs leading-relaxed text-[#64748B]">
                            {description}
                        </p>
                    </div>
                </div>

                <div className="mt-6 flex items-center justify-end gap-2.5">
                    <button
                        type="button"
                        disabled={isLoading}
                        onClick={onClose}
                        className="rounded-lg border border-[#E2E8F0] px-3.5 py-2 text-xs font-medium text-[#2E3532] hover:bg-[#F8FAFC] transition-colors disabled:opacity-50"
                    >
                        {cancelText}
                    </button>

                    <button
                        type="button"
                        disabled={isLoading}
                        onClick={onConfirm}
                        className={`inline-flex items-center justify-center gap-1.5 rounded-lg px-3.5 py-2 text-xs font-semibold text-white transition-opacity disabled:opacity-50 ${
                            variant === "danger"
                                ? "bg-rose-600 hover:bg-rose-700"
                                : "bg-[#2E3532] hover:bg-[#1E293B]"
                        }`}
                    >
                        {isLoading && <Loader2 className="h-3.5 w-3.5 animate-spin text-white" />}
                        <span>{confirmText}</span>
                    </button>
                </div>
            </div>
        </div>
    );
}