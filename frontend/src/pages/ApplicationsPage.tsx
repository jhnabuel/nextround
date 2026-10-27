
import { Plus, Search, ArrowUpDown } from "lucide-react";
import { useState } from "react";

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
    const [searchQuery, setSearchQuery] = useState("");
    const [selectedStatus, setSelectedStatus] = useState("ALL");
    const [sortBy, setSortBy] = useState("createdAt,desc");

    // Mock total; replace with API data in Sprint 3.
    const totalApplications = 24;

    const handleOpenAddModal = () => {
        // Connect to CreateApplicationModal later.
        console.log("Open Add Application modal");
    };

    return (
        <div className="flex h-full min-h-0 flex-col gap-5">
            <header className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                <div>
                    <div className="flex flex-wrap items-center gap-3">
                        <h1 className="text-2xl font-semibold tracking-tight text-foreground">
                            Applications
                        </h1>
                        <span className="rounded-full border border-border bg-card px-2.5 py-1 text-xs font-medium text-muted-foreground">
              {totalApplications} total
            </span>
                    </div>

                    <p className="mt-1 text-sm text-muted-foreground">
                        Track and advance active job opportunities across your pipeline.
                    </p>
                </div>

                <button
                    type="button"
                    onClick={handleOpenAddModal}
                    className="inline-flex shrink-0 items-center justify-center gap-2 rounded-[10px] bg-primary px-4 py-2.5 text-sm font-medium text-primary-foreground shadow-sm transition-colors hover:bg-primary/90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
                >
                    <Plus className="size-4" />
                    Add Application
                </button>
            </header>

            <section
                aria-label="Application filters"
                className="flex flex-col gap-3 rounded-xl border border-border bg-card p-3 sm:flex-row sm:items-center"
            >
                <div className="relative min-w-0 flex-1">
                    <Search className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />

                    <input
                        type="search"
                        value={searchQuery}
                        onChange={(event) => setSearchQuery(event.target.value)}
                        placeholder="Search by job title or company..."
                        aria-label="Search applications by job title or company"
                        className="w-full rounded-lg border border-input bg-background py-2 pl-9 pr-3 text-sm text-foreground outline-none transition-colors placeholder:text-muted-foreground focus-visible:border-ring focus-visible:ring-2 focus-visible:ring-ring/20"
                    />
                </div>

                <div className="flex flex-wrap items-center gap-2">
                    <select
                        value={selectedStatus}
                        onChange={(event) => setSelectedStatus(event.target.value)}
                        aria-label="Filter by application status"
                        className="min-w-36 rounded-lg border border-input bg-background px-3 py-2 text-sm text-foreground outline-none focus-visible:ring-2 focus-visible:ring-ring/20"
                    >
                        {STATUS_OPTIONS.map((option) => (
                            <option key={option.value} value={option.value}>
                                {option.label}
                            </option>
                        ))}
                    </select>

                    <div className="flex items-center gap-2 border-l border-border pl-2">
                        <ArrowUpDown className="size-4 shrink-0 text-muted-foreground" />

                        <select
                            value={sortBy}
                            onChange={(event) => setSortBy(event.target.value)}
                            aria-label="Sort applications"
                            className="max-w-40 rounded-lg border border-input bg-background px-3 py-2 text-sm text-foreground outline-none focus-visible:ring-2 focus-visible:ring-ring/20"
                        >
                            <option value="createdAt,desc">Newest First</option>
                            <option value="createdAt,asc">Oldest First</option>
                            <option value="appliedDate,desc">Recently Applied</option>
                            <option value="salaryMax,desc">Highest Salary</option>
                        </select>
                    </div>
                </div>
            </section>

            <section
                aria-label="Applications Kanban board"
                className="flex min-h-0 flex-1 flex-col overflow-hidden rounded-xl border border-border bg-muted/40 p-3 sm:p-4"
            >
                <div className="flex min-h-0 flex-1 items-center justify-center rounded-lg border border-dashed border-border bg-background/50">
                    <div className="text-center">
                        <p className="text-sm font-medium text-foreground">
                            Kanban board
                        </p>
                        <p className="mt-1 text-sm text-muted-foreground">
                            Your application columns will appear here.
                        </p>
                    </div>
                </div>
            </section>
        </div>
    );
}
