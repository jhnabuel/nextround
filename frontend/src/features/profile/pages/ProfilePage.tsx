import { User, Mail, Shield, Save } from "lucide-react";
import { PageContainer, PageHeader } from "../../../components/layout/PageContainer";
import { useAuth } from "../../../context/useAuth";

export default function ProfilePage() {
    const { user } = useAuth();

    return (
        <PageContainer className="max-w-4xl gap-6">
            <PageHeader
                title="Profile Settings"
                description="Update your candidate profile, contact information, and authentication credentials."
                actions={
                    <button
                        type="button"
                        className="inline-flex items-center gap-2 rounded-[10px] bg-[#2E3532] px-4 py-2 text-sm font-medium text-white shadow-xs hover:bg-[#1E2321] transition-colors outline-none focus-visible:ring-2 focus-visible:ring-[#7E9181] focus-visible:ring-offset-2"
                    >
                        <Save className="size-4" />
                        Save Changes
                    </button>
                }
            />

            <div className="bg-white border border-[#E2E8F0] rounded-[10px] p-6 shadow-xs space-y-6">
                <div>
                    <h3 className="text-sm font-semibold text-[#1E293B]">Personal Details</h3>
                    <p className="text-xs text-[#64748B] mt-0.5">
                        This information will be associated with outgoing applications.
                    </p>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div>
                        <label className="block text-xs font-medium text-[#1E293B] mb-1.5">
                            First Name
                        </label>
                        <div className="relative">
                            <User className="w-4 h-4 text-[#94A3B8] absolute left-3 top-1/2 -translate-y-1/2" />
                            <input
                                type="text"
                                defaultValue={user?.firstName ?? "Candidate"}
                                className="w-full pl-9 pr-3 py-2 text-sm bg-[#F8FAFC] border border-[#E2E8F0] rounded-lg outline-none focus-visible:ring-2 focus-visible:ring-[#7E9181]"
                            />
                        </div>
                    </div>

                    <div>
                        <label className="block text-xs font-medium text-[#1E293B] mb-1.5">
                            Last Name
                        </label>
                        <input
                            type="text"
                            defaultValue={user?.lastName ?? "User"}
                            className="w-full px-3 py-2 text-sm bg-[#F8FAFC] border border-[#E2E8F0] rounded-lg outline-none focus-visible:ring-2 focus-visible:ring-[#7E9181]"
                        />
                    </div>
                </div>

                <div>
                    <label className="block text-xs font-medium text-[#1E293B] mb-1.5">
                        Email Address
                    </label>
                    <div className="relative">
                        <Mail className="w-4 h-4 text-[#94A3B8] absolute left-3 top-1/2 -translate-y-1/2" />
                        <input
                            type="email"
                            disabled
                            defaultValue={user?.email ?? "candidate@nextround.dev"}
                            className="w-full pl-9 pr-3 py-2 text-sm bg-[#F1F5F9] border border-[#E2E8F0] rounded-lg text-[#64748B] cursor-not-allowed"
                        />
                    </div>
                </div>

                <div className="pt-4 border-t border-[#E2E8F0] flex items-center justify-between text-xs text-[#64748B]">
          <span className="flex items-center gap-1.5 font-medium">
            <Shield className="w-4 h-4 text-[#7E9181]" />
            Session protected with JWT Bearer authentication
          </span>
                </div>
            </div>
        </PageContainer>
    );
}