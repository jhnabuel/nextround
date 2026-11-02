import { useLocation } from "react-router-dom";
import { Menu } from "lucide-react";
import UserMenu from "./UserMenu";

const ROUTE_TITLES: Record<string, string> = {
    "/dashboard": "Dashboard",
    "/applications": "Applications",
    "/companies": "Companies",
    "/profile": "Profile",
};

interface AppHeaderProps {
    onOpenMobileSidebar: () => void;
}

export function AppHeader({ onOpenMobileSidebar }: AppHeaderProps) {
    const location = useLocation();
    const currentTitle = ROUTE_TITLES[location.pathname] ?? "Workspace";

    return (
        <header className="h-16 px-4 sm:px-6 border-b border-[#E2E8F0] bg-white flex items-center justify-between shrink-0 select-none">
            {/* 1. Mobile Hamburger + Page Title */}
            <div className="flex items-center gap-3">
                <button
                    type="button"
                    onClick={onOpenMobileSidebar}
                    aria-label="Open sidebar menu"
                    className="md:hidden p-2 -ml-2 rounded-lg text-[#64748B] hover:text-[#2E3532] hover:bg-[#F1F5F3] outline-none focus-visible:ring-2 focus-visible:ring-[#7E9181] cursor-pointer"
                >
                    <Menu className="w-5 h-5" />
                </button>

                <h2 className="text-base font-semibold text-[#2E3532] tracking-tight">
                    {currentTitle}
                </h2>
            </div>

            {/* 2. Extracted User Avatar & Dropdown Menu */}
            <UserMenu />
        </header>
    );
}

export default AppHeader;