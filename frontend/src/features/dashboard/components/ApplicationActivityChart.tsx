import {
    Bar,
    BarChart,
    CartesianGrid,
    ResponsiveContainer,
    Tooltip,
    XAxis,
    YAxis,
} from "recharts";
import type { ActivityGranularity, ActivityPoint } from "../types/dashboard.types";

interface ApplicationActivityChartProps {
    data: ActivityPoint[];
    granularity: ActivityGranularity;
    onGranularityChange: (granularity: ActivityGranularity) => void;
    isLoading: boolean;
    isPlaceholderData?: boolean;
}

export function ApplicationActivityChart({
                                             data,
                                             granularity,
                                             onGranularityChange,
                                             isLoading,
                                             isPlaceholderData = false,
                                         }: ApplicationActivityChartProps) {
    const parsePeriodDate = (period: string): Date => {
        const [year, month, day = 1] = period.split("-").map(Number);
        return new Date(year, month - 1, day);
    };

    const formatTick = (period: string): string => {
        const date = parsePeriodDate(period);
        if (granularity === "WEEK") {
            return date.toLocaleDateString("en-US", {
                month: "short",
                day: "numeric",
            }); // e.g., "Nov 16"
        }
        return date.toLocaleDateString("en-US", {
            month: "short",
            year: "2-digit",
        }); // e.g., "Nov 26"
    };

    const formatTooltipLabel = (label: unknown): string => {
        const period = String(label ?? "");
        if (!period) return "";
        const date = parsePeriodDate(period);

        if (granularity === "WEEK") {
            return `Week of ${date.toLocaleDateString("en-US", {
                month: "short",
                day: "numeric",
                year: "numeric",
            })}`;
        }
        return date.toLocaleDateString("en-US", {
            month: "long",
            year: "numeric",
        });
    };

    return (
        <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
            {/* Header + Segmented Toggle */}
            <div className="mb-6 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <div>
                    <h2 className="text-base font-semibold text-slate-900">
                        Application Activity
                    </h2>
                    <p className="text-xs text-slate-500">
                        {granularity === "WEEK"
                            ? "Submitted applications over the last 12 weeks"
                            : "Submitted applications over the last 6 months"}
                    </p>
                </div>

                <div className="inline-flex rounded-lg border border-slate-200 bg-slate-50 p-1">
                    {(["WEEK", "MONTH"] as const).map((option) => {
                        const active = granularity === option;
                        return (
                            <button
                                key={option}
                                type="button"
                                onClick={() => onGranularityChange(option)}
                                className={`rounded-md px-3 py-1.5 text-xs font-medium transition-colors ${
                                    active
                                        ? "bg-white text-slate-900 shadow-sm"
                                        : "text-slate-600 hover:text-slate-900"
                                }`}
                            >
                                {option === "WEEK" ? "12 Weeks" : "6 Months"}
                            </button>
                        );
                    })}
                </div>
            </div>

            {/* Chart Body (Fixed height is required for ResponsiveContainer) */}
            {isLoading ? (
                <div className="h-72 w-full animate-pulse rounded-lg bg-slate-100" />
            ) : (
                <div
                    className={`h-72 w-full transition-opacity ${
                        isPlaceholderData ? "opacity-50" : "opacity-100"
                    }`}
                >
                    <ResponsiveContainer width="100%" height="100%">
                        <BarChart data={data} margin={{ top: 8, right: 8, left: -16, bottom: 0 }}>
                            <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                            <XAxis
                                dataKey="period"
                                tickFormatter={formatTick}
                                tick={{ fontSize: 12, fill: "#64748b" }}
                                axisLine={false}
                                tickLine={false}
                            />
                            <YAxis
                                allowDecimals={false}
                                tick={{ fontSize: 12, fill: "#64748b" }}
                                axisLine={false}
                                tickLine={false}
                            />
                            <Tooltip
                                cursor={{ fill: "#f1f5f9" }}
                                contentStyle={{
                                    borderRadius: "0.5rem",
                                    border: "1px solid #e2e8f0",
                                    fontSize: "0.75rem",
                                }}
                                labelFormatter={formatTooltipLabel}
                                formatter={(value) => {
                                    const count = Number(value ?? 0);
                                    return[
                                        `${count} ${count === 1 ? "application" : "applications"}`,
                                        "Submitted",
                                    ];
                                }}
                            />
                            <Bar
                                dataKey="count"
                                fill="#2563eb"
                                radius={[6, 6, 0, 0]}
                                maxBarSize={42}
                            />
                        </BarChart>
                    </ResponsiveContainer>
                </div>
            )}
        </div>
    );
}