import { useState } from "react";
import { Link } from "react-router-dom";
import { Plus, AlertCircle } from "lucide-react";
import { DashboardStatsGrid } from "../components/DashboardStatsGrid";
import { ApplicationActivityChart } from "../components/ApplicationActivityChart";
import type { ActivityGranularity, ActivityPoint } from "../types/dashboard.types";
// TODO: Import useDashboardStats and useDashboardActivity from "../hooks/useDashboard"

export default function DashboardPage() {
    const [granularity, setGranularity] = useState<ActivityGranularity>("WEEK");

    // TODO: Wire up useDashboardStats() and useDashboardActivity(granularity)
    const stats = undefined;
    const isStatsLoading = false;
    const isStatsError = false;
    const refetchStats = () => {};

    const activityData: ActivityPoint[] = [];
    const isActivityLoading = false;
    const isPlaceholderData = false;
    const isActivityError = false;
    const refetchActivity = () => {};

    // TODO: Derive zero-state check (e.g., !isStatsLoading && !isStatsError && stats?.totalApplications === 0)
    const isZeroState = false;

    return (
        <div className="mx-auto max-w-7xl space-y-6 px-4 py-8 sm:px-6 lg:px-8">
            {/* Page Header */}
            <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <div>
                    <h1 className="text-2xl font-bold tracking-tight text-slate-900">
                        Dashboard
                    </h1>
                    <p className="text-sm text-slate-500">
                        Track your job search pipeline and weekly application momentum.
                    </p>
                </div>

                <Link
                    to="/applications"
                    className="inline-flex items-center gap-2 self-start rounded-lg bg-slate-900 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-slate-800"
                >
                    <Plus className="h-4 w-4" />
                    Go to Applications
                </Link>
            </div>

            {/* Inline Error Banner */}
            {(isStatsError || isActivityError) && (
                <div className="flex items-center justify-between rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-800">
                    <div className="flex items-center gap-2">
                        <AlertCircle className="h-4 w-4 shrink-0" />
                        <span>Could not load dashboard metrics. Please try again.</span>
                    </div>
                    <button
                        type="button"
                        onClick={() => {
                            if (isStatsError) refetchStats();
                            if (isActivityError) refetchActivity();
                        }}
                        className="rounded-md bg-white px-3 py-1 text-xs font-medium text-rose-700 shadow-sm hover:bg-rose-100"
                    >
                        Retry
                    </button>
                </div>
            )}

            {/* KPI Row */}
            <DashboardStatsGrid stats={stats} isLoading={isStatsLoading} />

            {/* Zero-State Banner OR Activity Chart */}
            {isZeroState ? (
                <div className="flex flex-col items-center justify-center rounded-xl border border-dashed border-slate-300 bg-white px-6 py-16 text-center">
                    <h3 className="text-base font-semibold text-slate-900">
                        No submitted applications yet
                    </h3>
                    <p className="mt-1 max-w-sm text-sm text-slate-500">
                        Move an item out of your Wishlist or log your first application to unlock
                        activity trends.
                    </p>
                    <Link
                        to="/applications"
                        className="mt-5 inline-flex items-center gap-2 rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
                    >
                        <Plus className="h-4 w-4" />
                        Add Your First Application
                    </Link>
                </div>
            ) : (
                <ApplicationActivityChart
                    data={activityData}
                    granularity={granularity}
                    onGranularityChange={setGranularity}
                    isLoading={isActivityLoading}
                    isPlaceholderData={isPlaceholderData}
                />
            )}
        </div>
    );
}