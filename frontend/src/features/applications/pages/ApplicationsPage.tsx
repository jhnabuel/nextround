import { Plus, Search, ArrowUpDown } from "lucide-react";
import { useMemo, useState } from "react";

import type { KanbanApplication } from "../types/kanban.types";
import { KanbanBoard } from "../components/KanbanBoard";

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

const MOCK_APPLICATIONS: KanbanApplication[] = [
    {
        id: "app-1",
        jobTitle: "Junior Software Engineer",
        companyName: "Canva",
        location: "Manila, PH",
        workLocation: "HYBRID",
        salaryMin: 55000,
        salaryMax: 75000,
        salaryCurrency: "PHP",
        appliedDate: "2026-10-15",
        status: "INTERVIEWING",
    },
    {
        id: "app-2",
        jobTitle: "Backend Java Developer",
        companyName: "Stripe",
        location: "Singapore",
        workLocation: "REMOTE",
        salaryMin: 90000,
        salaryMax: 120000,
        salaryCurrency: "USD",
        appliedDate: "2026-10-18",
        status: "APPLIED",
    },
    {
        id: "app-3",
        jobTitle: "Full Stack Engineer (React/Spring)",
        companyName: "Maya",
        location: "Taguig, PH",
        workLocation: "HYBRID",
        salaryMin: 60000,
        salaryMax: 85000,
        salaryCurrency: "PHP",
        appliedDate: "2026-10-20",
        status: "WISHLIST",
        createdAt: "2026-10-01"
    },
    {
        id: "app-4",
        jobTitle: "Frontend Developer",
        companyName: "Linear",
        workLocation: "REMOTE",
        salaryMin: 80000,
        salaryMax: 100000,
        salaryCurrency: "USD",
        status: "WISHLIST",
        createdAt: "2026-10-12"
    },
    {
        id: "app-5",
        jobTitle: "Associate Software Engineer",
        companyName: "Accenture",
        location: "Cebu, PH",
        workLocation: "ON_SITE",
        appliedDate: "2026-09-28",
        status: "REJECTED",
    },
    {
        id: "app-6",
        jobTitle: "Software Engineer I",
        companyName: "Vercel",
        workLocation: "REMOTE",
        salaryMin: 110000,
        salaryMax: 130000,
        salaryCurrency: "USD",
        appliedDate: "2026-10-02",
        status: "WISHLIST",
        createdAt: "2026-10-03"
    },
];

export default function ApplicationsPage() {
    const [applications] = useState<KanbanApplication[]>(MOCK_APPLICATIONS);
    const [searchQuery, setSearchQuery] = useState("");
    const [selectedStatus, setSelectedStatus] = useState<StatusFilter>("ALL")
    const [sortBy, setSortBy] = useState("createdAt,desc");

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
                if (sortBy === 'createdAt,desc'){
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
        console.log("Open Add Application modal");
    };

    const handleCardClick = (id: string) => {
        console.log("Selected application ID:", id);
    };

    return (
        <div className="flex h-full min-h-0 flex-col gap-4">
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
                        onChange={(event) => setSelectedStatus(event.target.value)}
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
                <KanbanBoard
                    applications={filteredApplications}
                    onCardClick={handleCardClick}
                />
            </div>
        </div>
    );
}