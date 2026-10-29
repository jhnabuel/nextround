import { Calendar, MapPin, Building2 } from "lucide-react";
import type { KanbanApplication } from "../types/kanban.types";
import { formatSalaryRange, formatDate } from "../../../lib/formatters";

interface ApplicationCardProps {
    application: KanbanApplication;
    onClick?: (id: string) => void;
}

const WORK_LOCATION_LABELS = {
    REMOTE: "Remote",
    HYBRID: "Hybrid",
    ON_SITE: "On-site",
};

export function ApplicationCard({ application, onClick }: ApplicationCardProps) {
    const {
        id,
        jobTitle,
        companyName,
        location,
        workLocation,
        salaryMin,
        salaryMax,
        salaryCurrency = "USD",
        appliedDate,
    } = application;

    const formattedSalary = formatSalaryRange(salaryMin, salaryMax, salaryCurrency);
    const formattedDate = formatDate(appliedDate);

    return (
        <article
            onClick={() => onClick?.(id)}
            className="group relative bg-white border border-[#E2E8F0] rounded-[10px] p-4 shadow-[0_1px_2px_rgba(0,0,0,0.04)] hover:border-[#7E9181] hover:shadow-[0_2px_8px_rgba(46,53,50,0.08)] transition-all duration-150 cursor-pointer select-none"
        >
            {/* Header: Company Name */}
            <div className="flex items-center gap-1.5 mb-1.5 min-w-0 text-[#64748B]">
                <Building2 className="w-3.5 h-3.5 shrink-0 text-[#7E9181]" />
                <span className="text-[13px] font-medium text-[#64748B] truncate">
          {companyName}
        </span>
            </div>

            {/* Main Title */}
            <h4 className="text-[15px] font-semibold text-[#1E293B] group-hover:text-[#2E3532] leading-snug line-clamp-2 mb-2.5">
                {jobTitle}
            </h4>

            {/* Badges: Work Arrangement & Location */}
            <div className="flex flex-wrap items-center gap-1.5 mb-3">
        <span className="inline-flex items-center text-[11px] font-medium px-2 py-0.5 rounded-full bg-[#F1F5F3] text-[#2E3532] border border-[#E2E8F0]">
          {WORK_LOCATION_LABELS[workLocation]}
        </span>

                {location && (
                    <span className="inline-flex items-center gap-1 text-[11px] text-[#64748B] px-1.5 py-0.5">
            <MapPin className="w-3 h-3 text-[#64748B]" />
            <span className="truncate max-w-[120px]">{location}</span>
          </span>
                )}
            </div>

            {/* Footer: Dynamic Currency Salary & Localized Date */}
            <div className="pt-2.5 border-t border-[#E2E8F0] flex items-center justify-between text-[12px] text-[#64748B]">
                {formattedSalary ? (
                    <div className="flex items-center gap-1 font-medium text-[#2E3532]">
                        <span>{formattedSalary}</span>
                    </div>
                ) : (
                    <span className="text-[11px] text-[#94A3B8]">Salary unspecified</span>
                )}

                {formattedDate && (
                    <div className="flex items-center gap-1 text-[11px] text-[#64748B]">
                        <Calendar className="w-3 h-3 text-[#94A3B8]" />
                        <span>{formattedDate}</span>
                    </div>
                )}
            </div>
        </article>
    );
}