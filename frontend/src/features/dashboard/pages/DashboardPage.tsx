import { Briefcase, Send, CheckCircle2, TrendingUp } from "lucide-react";
import { PageContainer, PageHeader } from "../../../components/layout/PageContainer";

const STATS = [
    { label: "Active Applications", value: "18", change: "+4 this week", icon: Briefcase },
    { label: "Interviews Scheduled", value: "3", change: "Next: Tomorrow", icon: Send },
    { label: "Offers Received", value: "1", change: "Action required", icon: CheckCircle2 },
    { label: "Response Rate", value: "38%", change: "+5% vs last month", icon: TrendingUp },
];

export default function DashboardPage() {
    return (
        <PageContainer className="gap-6">
            <PageHeader
                title="Dashboard"
                description="High-level overview of your active job search metrics and schedule."
            />

            {/* Metric Cards Grid */}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                {STATS.map((stat) => {
                    const Icon = stat.icon;
                    return (
                        <div
                            key={stat.label}
                            className="bg-white border border-[#E2E8F0] rounded-[10px] p-5 shadow-xs"
                        >
                            <div className="flex items-center justify-between text-[#64748B]">
                <span className="text-xs font-medium uppercase tracking-wider">
                  {stat.label}
                </span>
                                <div className="p-2 rounded-lg bg-[#F1F5F3] text-[#7E9181]">
                                    <Icon className="w-4 h-4" />
                                </div>
                            </div>
                            <p className="text-2xl font-bold text-[#1E293B] mt-2 leading-none">
                                {stat.value}
                            </p>
                            <p className="text-xs text-[#7E9181] font-medium mt-2">
                                {stat.change}
                            </p>
                        </div>
                    );
                })}
            </div>

            {/* Activity Placeholder */}
            <div className="bg-white border border-[#E2E8F0] rounded-[10px] p-6 shadow-xs min-h-[300px] flex items-center justify-center text-center">
                <div>
                    <p className="text-sm font-semibold text-[#1E293B]">Recent Activity</p>
                    <p className="text-xs text-[#64748B] mt-1">
                        Application milestones and calendar interviews will be displayed here.
                    </p>
                </div>
            </div>
        </PageContainer>
    );
}