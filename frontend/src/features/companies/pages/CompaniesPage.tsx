import { Plus, Search, Building2 } from "lucide-react";
import { PageContainer, PageHeader } from "../../../components/layout/PageContainer";

export default function CompaniesPage() {
    return (
        <PageContainer className="gap-6">
            <PageHeader
                title="Companies"
                description="Manage company details, recruiter contacts, and organization notes."
                badge={
                    <span className="rounded-full border border-[#E2E8F0] bg-white px-2.5 py-0.5 text-xs font-semibold text-[#2E3532]">
            6 Saved
          </span>
                }
                actions={
                    <button
                        type="button"
                        className="inline-flex items-center gap-2 rounded-[10px] bg-[#2E3532] px-4 py-2 text-sm font-medium text-white shadow-xs hover:bg-[#1E2321] transition-colors outline-none focus-visible:ring-2 focus-visible:ring-[#7E9181] focus-visible:ring-offset-2"
                    >
                        <Plus className="size-4" />
                        Add Company
                    </button>
                }
            />

            <section className="bg-white border border-[#E2E8F0] rounded-[10px] p-3 shadow-xs">
                <div className="relative">
                    <Search className="w-4 h-4 text-[#94A3B8] absolute left-3 top-1/2 -translate-y-1/2" />
                    <input
                        type="search"
                        placeholder="Search saved companies..."
                        className="w-full pl-9 pr-3 py-1.5 text-sm bg-[#F8FAFC] border border-[#E2E8F0] rounded-lg outline-none focus-visible:ring-2 focus-visible:ring-[#7E9181]"
                    />
                </div>
            </section>

            <div className="bg-white border border-dashed border-[#CBD5E1] rounded-[10px] p-12 text-center">
                <Building2 className="w-8 h-8 text-[#7E9181] mx-auto mb-2 opacity-80" />
                <p className="text-sm font-medium text-[#1E293B]">Company directory integration</p>
                <p className="text-xs text-[#64748B] mt-1">
                    Connected directly to your job applications in Sprint 3.
                </p>
            </div>
        </PageContainer>
    );
}