import { Plus, Search, ArrowUpDown, Loader2 } from "lucide-react";
import { useMemo, useState } from "react";

import type { KanbanApplication } from "../types/kanban.types";
import type { JobApplicationResponse, ApplicationStatus, UpdateJobApplicationRequest } from "../types/application.types";
import { KanbanBoard } from "../components/KanbanBoard";
import { PageContainer } from "../../../components/layout/PageContainer";
import { useApplications, useUpdateApplication } from "../hooks/useApplications";
import { ApplicationFormDialog } from "../components/ApplicationFormDialog";

type StatusFilter = KanbanApplication["status"] | "ALL";

const STATUS_OPTIONS = [
    { value: "ALL", label: "All Statuses" },
    { value: "WISHLIST", label: "Wishlist" },
    { value: "APPLIED", label: "Applied" },
    { value: "INTERVIEWING", label: "Interviewing" },
    { value: "OFFER_RECEIVED", label: "Offer Received" },
    { value: "ACCEPTED", label: "Accepted" },
    { value: "REJECTED", label: "Rejected" },
    { value: "WITHDRAWN", label: "Withdrawn" },
    { value: "GHOSTED", label: "Ghosted" },
];

export default function ApplicationsPage() {
    const { data: rawApplications = [], isLoading, isError, error } = useApplications();
    const [searchQuery, setSearchQuery] = useState("");
    const [selectedStatus, setSelectedStatus] = useState<StatusFilter>("ALL");
    const [sortBy, setSortBy] = useState("createdAt,desc");

    const updateMutation = useUpdateApplication();

    // Dialog state for create/edit operations
    const [isDialogOpen, setIsDialogOpen] = useState(false);
    const [selectedApp, setSelectedApp] = useState<JobApplicationResponse | null>(null);

    function formatSalary(min?: number, max?: number, currency = "PHP"): string | undefined {
        if (min == null && max == null) return undefined;

        const fmt = new Intl.NumberFormat("en-US", { maximumFractionDigits: 0 });

        if (min != null && max != null) {
            return `${currency} ${fmt.format(min)} – ${fmt.format(max)}`;
        }
        if (min != null) {
            return `${currency} ${fmt.format(min)}+`;
        }
        return `Up to ${currency} ${fmt.format(max!)}`;
    }


    // Map backend JobApplicationResponse to KanbanApplication shape
    const applications = useMemo<KanbanApplication[]>(() => {
        if (!Array.isArray(rawApplications)) {
            return [];
        }

        return rawApplications.map((app) => ({
            id: app.id,
            jobTitle: app.jobTitle,
            companyName: app.company.companyName,
            location: app.company.location,
            status: app.applicationStatus,
            workLocation: app.workLocation,
            salaryMin: app.salaryMin,
            salaryMax: app.salaryMax,
            salaryCurrency: app.currency,
            formattedSalary: formatSalary(app.salaryMin, app.salaryMax, app.currency || "PHP"),
            appliedDate: app.appliedDate,
            formattedDate: app.appliedDate
                ? new Date(app.appliedDate).toLocaleDateString("en-US", {
                    month: "short",
                    day: "numeric",
                })
                : undefined,
            jobUrl: app.jobUrl,
            createdAt: app.createdAt,
            updatedAt: app.updatedAt,
        }));
    }, [rawApplications]);

    const filteredApplications = useMemo(() => {
        const query = searchQuery.trim().toLowerCase();
        return applications
            .filter((app) => {
                const matchesSearch =
                    !query ||
                    app.jobTitle.toLowerCase().includes(query) ||
                    app.companyName.toLowerCase().includes(query);

                const matchesStatus =
                    selectedStatus === "ALL" || app.status === selectedStatus;

                return matchesSearch && matchesStatus;
            })
            .sort((a, b) => {
                if (sortBy === "createdAt,desc") {
                    return (b.createdAt ?? "").localeCompare(a.createdAt ?? "");
                }
                if (sortBy === "appliedDate,desc") {
                    return (b.appliedDate ?? "").localeCompare(a.appliedDate ?? "");
                }
                if (sortBy === "salaryMax,desc") {
                    return (b.salaryMax ?? 0) - (a.salaryMax ?? 0);
                }
                return 0;
            });
    }, [applications, searchQuery, selectedStatus, sortBy]);

    const handleOpenAddModal = () => {
        setSelectedApp(null);
        setIsDialogOpen(true);
    };

    const handleCardClick = (id: string) => {
        const found = rawApplications.find((app) => app.id === id) || null;
        setSelectedApp(found);
        setIsDialogOpen(true);
    };

    const handleCloseDialog = () => {
        setIsDialogOpen(false);
        setSelectedApp(null);
    };

    const handleStatusChange = (id: string, newStatus: ApplicationStatus) => {
        const existingApp = rawApplications.find((app) => app.id === id);
        if(!existingApp || existingApp.applicationStatus === newStatus){
            return;
        }

        const payload: UpdateJobApplicationRequest = {
            companyId: existingApp.company.id,
            jobTitle: existingApp.jobTitle,
            jobUrl: existingApp.jobUrl || undefined,
            applicationStatus: newStatus,
            workLocation: existingApp.workLocation,
            salaryMin: existingApp.salaryMin ?? undefined,
            salaryMax: existingApp.salaryMax ?? undefined,
            currency: existingApp.currency || "PHP",
            appliedDate: existingApp.appliedDate || undefined,
        };

        const statusLabel =
            STATUS_OPTIONS.find((opt) => opt.value === newStatus)?.label ?? newStatus;

        updateMutation.mutate({
            id,
            payload,
            successMessage: `Moved ${existingApp.jobTitle} to ${statusLabel}`,
        });
    };


    return (
        <PageContainer fluid={true} className="gap-4">
            {/* 1. Header */}
            <header className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between shrink-0">
                <div>
                    <div className="flex flex-wrap items-center gap-2.5">
                        <h1 className="text-2xl font-semibold tracking-tight text-foreground">
                            Applications
                        </h1>
                        <span className="rounded-full border border-border bg-card px-2.5 py-0.5 text-xs font-medium text-muted-foreground">
                            {filteredApplications.length} of {applications.length}
                        </span>
                    </div>

                    <p className="mt-1 text-sm text-muted-foreground">
                        Track and advance active job opportunities across your pipeline.
                    </p>
                </div>

                <button
                    type="button"
                    onClick={handleOpenAddModal}
                    className="inline-flex shrink-0 items-center justify-center gap-2 rounded-[10px] bg-primary px-4 py-2 text-sm font-medium text-primary-foreground shadow-xs transition-colors hover:bg-primary/90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
                >
                    <Plus className="size-4" />
                    Add Application
                </button>
            </header>

            {/* 2. Filter & Sort Toolbar */}
            <section
                aria-label="Application filters"
                className="flex flex-col gap-3 rounded-xl border border-border bg-card p-3 sm:flex-row sm:items-center shrink-0"
            >
                <div className="relative min-w-0 flex-1">
                    <Search className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
                    <input
                        type="search"
                        value={searchQuery}
                        onChange={(event) => setSearchQuery(event.target.value)}
                        placeholder="Search by job title or company..."
                        aria-label="Search applications by job title or company"
                        className="w-full rounded-lg border border-input bg-background py-1.5 pl-9 pr-3 text-sm text-foreground outline-none transition-colors placeholder:text-muted-foreground focus-visible:border-ring focus-visible:ring-2 focus-visible:ring-ring/20"
                    />
                </div>

                <div className="flex flex-wrap items-center gap-2">
                    <select
                        value={selectedStatus}
                        onChange={(event) => setSelectedStatus(event.target.value as StatusFilter)}
                        aria-label="Filter by application status"
                        className="rounded-lg border border-input bg-background px-3 py-1.5 text-sm text-foreground outline-none focus-visible:ring-2 focus-visible:ring-ring/20 cursor-pointer"
                    >
                        {STATUS_OPTIONS.map((option) => (
                            <option key={option.value} value={option.value}>
                                {option.label}
                            </option>
                        ))}
                    </select>

                    <div className="flex items-center gap-1.5 border-l border-border pl-2">
                        <ArrowUpDown className="size-3.5 shrink-0 text-muted-foreground" />
                        <select
                            value={sortBy}
                            onChange={(event) => setSortBy(event.target.value)}
                            aria-label="Sort applications"
                            className="rounded-lg border border-input bg-background px-3 py-1.5 text-sm text-foreground outline-none focus-visible:ring-2 focus-visible:ring-ring/20 cursor-pointer"
                        >
                            <option value="createdAt,desc">Newest First</option>
                            <option value="appliedDate,desc">Recently Applied</option>
                            <option value="salaryMax,desc">Highest Salary</option>
                        </select>
                    </div>
                </div>
            </section>

            {/* 3. Live Kanban Board Canvas */}
            <div className="flex-1 min-h-0 flex flex-col overflow-hidden">
                {isLoading ? (
                    <div className="flex h-64 flex-col items-center justify-center rounded-xl border border-border bg-card text-muted-foreground">
                        <Loader2 className="h-6 w-6 animate-spin text-primary mb-2" />
                        <span className="text-xs font-medium">Loading applications...</span>
                    </div>
                ) : isError ? (
                    <div className="rounded-lg border border-destructive/20 bg-destructive/10 p-4 text-xs text-destructive">
                        Failed to load applications: {(error as Error)?.message || "Unknown error"}
                    </div>
                ) : (
                    <KanbanBoard
                        applications={filteredApplications}
                        onCardClick={handleCardClick}
                        onStatusChange={handleStatusChange}
                    />
                )}
            </div>

            {/* 4. Create / Edit Application Dialog */}
            <ApplicationFormDialog
                isOpen={isDialogOpen}
                onClose={handleCloseDialog}
                initialData={selectedApp}
            />
        </PageContainer>
    );
}