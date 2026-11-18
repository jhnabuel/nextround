import { useEffect, useState } from "react";
import {
    AlertCircle,
    Building2,
    Loader2,
    Plus,
    RefreshCw,
    Search,
    X,
} from "lucide-react";
import { useCompanies } from "../hooks/useCompanies";
import type { CompanyResponse } from "../types/company.types";
import { CompanyTable } from "../components/CompanyTable";
import { CompanyFormDialog } from "../components/CompanyFormDialog";


function useDebouncedValue<T>(value: T, delayMs = 300): T {
    const [debounced, setDebounced] = useState(value);

    useEffect(() => {
        const timer = window.setTimeout(() => setDebounced(value), delayMs);
        return () => window.clearTimeout(timer);
    }, [value, delayMs]);

    return debounced;
}

export default function CompaniesPage() {
    const [searchInput, setSearchInput] = useState("");
    const debouncedSearch = useDebouncedValue(searchInput, 300);

    const [isFormOpen, setIsFormOpen] = useState(false);
    const [editingCompany, setEditingCompany] = useState<CompanyResponse | null>(null);
    const [deletingCompany, setDeletingCompany] = useState<CompanyResponse | null>(null);

    const {
        data: companies = [],
        isLoading,
        isFetching,
        isError,
        refetch,
    } = useCompanies(debouncedSearch);

    const hasSearch = debouncedSearch.trim().length > 0;

    const handleOpenCreate = () => {
        setEditingCompany(null);
        setIsFormOpen(true);
    };

    const handleOpenEdit = (company: CompanyResponse) => {
        setEditingCompany(company);
        setIsFormOpen(true);
    };

    return (
        <div className="mx-auto max-w-5xl space-y-6 px-4 py-8 sm:px-6">
            {/* Header */}
            <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                <div>
                    <h1 className="text-2xl font-bold tracking-tight text-slate-900">Companies</h1>
                    <p className="mt-1 text-sm text-slate-600">
                        Manage companies linked to your job applications.
                    </p>
                </div>

                <button
                    type="button"
                    onClick={handleOpenCreate}
                    className="inline-flex items-center justify-center gap-2 rounded-lg bg-slate-900 px-4 py-2 text-sm font-medium text-white shadow-sm transition-colors hover:bg-slate-800"
                >
                    <Plus className="h-4 w-4" />
                    <span>Add Company</span>
                </button>
            </div>

            {/* Search Bar */}
            <div className="relative max-w-md">
                <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                <input
                    type="text"
                    value={searchInput}
                    onChange={(e) => setSearchInput(e.target.value)}
                    placeholder="Search companies by name..."
                    aria-label="Search companies"
                    className="w-full rounded-lg border border-slate-300 bg-white py-2 pl-9 pr-9 text-sm text-slate-900 placeholder:text-slate-400 focus:border-slate-900 focus:outline-none focus:ring-1 focus:ring-slate-900"
                />
                {isFetching && !isLoading ? (
                    <Loader2 className="absolute right-3 top-1/2 h-4 w-4 -translate-y-1/2 animate-spin text-slate-400" />
                ) : (
                    searchInput && (
                        <button
                            type="button"
                            onClick={() => setSearchInput("")}
                            aria-label="Clear search"
                            className="absolute right-2.5 top-1/2 -translate-y-1/2 rounded p-0.5 text-slate-400 hover:text-slate-700"
                        >
                            <X className="h-4 w-4" />
                        </button>
                    )
                )}
            </div>

            {/* Content States */}
            {isLoading ? (
                <div className="overflow-hidden rounded-lg border border-slate-200 bg-white">
                    <div className="divide-y divide-slate-200">
                        {Array.from({ length: 5 }).map((_, idx) => (
                            <div key={idx} className="flex items-center justify-between px-4 py-4">
                                <div className="space-y-2">
                                    <div className="h-4 w-40 animate-pulse rounded bg-slate-200" />
                                    <div className="h-3 w-28 animate-pulse rounded bg-slate-100" />
                                </div>
                                <div className="h-4 w-24 animate-pulse rounded bg-slate-100" />
                                <div className="h-4 w-24 animate-pulse rounded bg-slate-100" />
                                <div className="h-7 w-16 animate-pulse rounded bg-slate-100" />
                            </div>
                        ))}
                    </div>
                </div>
            ) : isError ? (
                <div className="flex flex-col items-center justify-center rounded-lg border border-red-200 bg-red-50/60 px-6 py-12 text-center">
                    <AlertCircle className="h-8 w-8 text-red-600" />
                    <h2 className="mt-3 text-base font-semibold text-slate-900">
                        Failed to load companies
                    </h2>
                    <p className="mt-1 text-sm text-slate-600">
                        Something went wrong while fetching your companies.
                    </p>
                    <button
                        type="button"
                        onClick={() => refetch()}
                        className="mt-4 inline-flex items-center gap-2 rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
                    >
                        <RefreshCw className="h-4 w-4" />
                        <span>Try again</span>
                    </button>
                </div>
            ) : companies.length === 0 ? (
                <div className="flex flex-col items-center justify-center rounded-lg border border-dashed border-slate-300 bg-white px-6 py-14 text-center">
                    <div className="rounded-full bg-slate-100 p-3 text-slate-500">
                        <Building2 className="h-6 w-6" />
                    </div>
                    <h2 className="mt-3 text-base font-semibold text-slate-900">
                        {hasSearch ? "No matching companies" : "No companies yet"}
                    </h2>
                    <p className="mt-1 max-w-sm text-sm text-slate-600">
                        {hasSearch
                            ? `No companies matched "${debouncedSearch}". Try a different search term.`
                            : "Add companies manually here or create them on the fly when logging a job application."}
                    </p>
                    {hasSearch ? (
                        <button
                            type="button"
                            onClick={() => setSearchInput("")}
                            className="mt-4 rounded-lg border border-slate-300 bg-white px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
                        >
                            Clear search
                        </button>
                    ) : (
                        <button
                            type="button"
                            onClick={handleOpenCreate}
                            className="mt-4 inline-flex items-center gap-2 rounded-lg bg-slate-900 px-4 py-2 text-sm font-medium text-white hover:bg-slate-800"
                        >
                            <Plus className="h-4 w-4" />
                            <span>Add your first company</span>
                        </button>
                    )}
                </div>
            ) : (
                <CompanyTable
                    companies={companies}
                    onEdit={handleOpenEdit}
                    onDelete={(company) => setDeletingCompany(company)}
                />
            )}

            {/* Task 5 & 6 Dialog Slots (isFormOpen, editingCompany, deletingCompany ready) */}
            {isFormOpen && (
                <CompanyFormDialog
                    key={editingCompany?.id ?? "create"}
                    open={isFormOpen}
                    company={editingCompany}
                    onClose={() => {
                        setIsFormOpen(false);
                        setEditingCompany(null);
                    }}
                />
            )}
            {deletingCompany && null}
        </div>
    );
}