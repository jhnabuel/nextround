import { LogOut } from "lucide-react";
import { useAuth } from "../../context/useAuth";

export function AppHeader() {
    const { user, logout } = useAuth();

    const userInitials = user?.firstName && user?.lastName
        ? `${user.firstName[0]}${user.lastName[0]}`.toUpperCase()
        : "NR";

    return (
        <header className="h-14 px-6 border-b border-[#E2E8F0] bg-white flex items-center justify-between shrink-0">
            {/* Optional Breadcrumb or Current Context area */}
            <div className="text-xs font-medium text-[#64748B]">
                Pipeline Workspace
            </div>

            {/* User Actions */}
            <div className="flex items-center gap-3">
                <div className="flex items-center gap-2.5">
                    <div className="w-8 h-8 rounded-full bg-[#F1F5F3] border border-[#E2E8F0] text-[#2E3532] text-xs font-semibold flex items-center justify-center select-none">
                        {userInitials}
                    </div>
                    {user && (
                        <div className="hidden sm:block text-left text-xs">
                            <p className="font-semibold text-[#1E293B] leading-none">
                                {user.firstName} {user.lastName}
                            </p>
                            <p className="text-[#64748B] text-[11px] mt-0.5 leading-none">
                                {user.email}
                            </p>
                        </div>
                    )}
                </div>

                <button
                    type="button"
                    onClick={logout}
                    title="Sign out"
                    aria-label="Sign out"
                    className="p-1.5 rounded-[8px] text-[#64748B] hover:text-[#991B1B] hover:bg-rose-50 transition-colors outline-none focus-visible:ring-2 focus-visible:ring-[#7E9181] focus-visible:ring-offset-2 cursor-pointer"
                >
                    <LogOut className="w-4 h-4" />
                </button>
            </div>
        </header>
    );
}

export default AppHeader;