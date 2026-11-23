import { Briefcase, Send, MessageSquare, Trophy, XCircle } from "lucide-react";
import type { DashboardStatsResponse } from "../types/dashboard.types";

interface DashboardStatsGridProps {
    stats?: DashboardStatsResponse;
    isLoading: boolean;
}

export function DashboardStatsGrid({ stats, isLoading }: DashboardStatsGridProps) {
    if (isLoading) {
        return (
            <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-5">
                {Array.from({ length: 5 }).map((_, i) => (
                    <div
                        key={i}
                        className="h-28 animate-pulse rounded-xl border border-slate-200 bg-slate-100 p-5"
                    />
                ))}
            </div>
        );
    }

    // TODO: Map your real `stats` fields into these 5 cards (fallback to 0 if undefined)
    const cards = [
        {
            label: "Total Applications",
            value: stats?.totalApplication ?? 0,
            icon: Briefcase,
            accent: "text-slate-700 bg-slate-100",
        },
        {
            label: "Applied",
            value: 0, // TODO: stats?.applied
            icon: Send,
            accent: "text-blue-700 bg-blue-50",
        },
        {
            label: "Interviewing",
            value: 0, // TODO: stats?.interviewing
            icon: MessageSquare,
            accent: "text-amber-700 bg-amber-50",
        },
        {
            label: "Offers",
            value: 0, // TODO: stats?.offers
            icon: Trophy,
            accent: "text-emerald-700 bg-emerald-50",
        },
        {
            label: "Rejected",
            value: 0, // TODO: stats?.rejected
            icon: XCircle,
            accent: "text-rose-700 bg-rose-50",
        },
    ];

    return (
        <div className="space-y-3">
            <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-5">
                {cards.map(({ label, value, icon: Icon, accent }) => (
                    <div
                        key={label}
                        className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm transition-shadow hover:shadow-md"
                    >
                        <div className="flex items-center justify-between">
                            <span className="text-xs font-medium text-slate-500">{label}</span>
                            <span className={`rounded-lg p-1.5 ${accent}`}>
                                <Icon className="h-4 w-4" />
                            </span>
                        </div>
                        <p className="mt-3 text-3xl font-semibold tabular-nums text-slate-900">
                            {value}
                        </p>
                    </div>
                ))}
            </div>

            {/* Optional secondary counts */}
            <div className="flex items-center gap-4 text-xs text-slate-500">
                <span>Withdrawn: {/* TODO: stats?.withdrawn ?? 0 */}0</span>
                <span>•</span>
                <span>Ghosted: {/* TODO: stats?.ghosted ?? 0 */}0</span>
            </div>
        </div>
    );
}