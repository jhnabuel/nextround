import { useState, useRef, useEffect } from "react";
import { useLocation, Link } from "react-router-dom";
import { User, LogOut, ChevronDown, Menu } from "lucide-react";
import { useAuth } from "../../context/useAuth";

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
    const { user } = useAuth();
    const location = useLocation();
    const [isDropdownOpen, setIsDropdownOpen] = useState(false);
    const dropdownRef = useRef<HTMLDivElement>(null);

    const currentTitle = ROUTE_TITLES[location.pathname] ?? "Workspace";

    const userInitials =
        user?.firstName && user?.lastName
            ? `${user.firstName[0]}${user.lastName[0]}`.toUpperCase()
            : "NR";

    useEffect(() => {
        function handleClickOutside(event: MouseEvent) {
            if (
                dropdownRef.current &&
                !dropdownRef.current.contains(event.target as Node)
            ) {
                setIsDropdownOpen(false);
            }
        }
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    const handleKeyDown = (e: React.KeyboardEvent) => {
        if (e.key === "Escape") {
            setIsDropdownOpen(false);
        }
    };

    const handleSignOutPlaceholder = () => {
        console.log("Sign out triggered (visual placeholder)");
        setIsDropdownOpen(false);
    };

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

            {/* 2. User Avatar & Dropdown Menu */}
            <div className="relative" ref={dropdownRef} onKeyDown={handleKeyDown}>
                <button
                    type="button"
                    onClick={() => setIsDropdownOpen((prev) => !prev)}
                    aria-expanded={isDropdownOpen}
                    aria-haspopup="menu"
                    aria-label="User account menu"
                    className="flex items-center gap-2.5 p-1.5 rounded-[10px] hover:bg-[#F8FAFC] transition-colors outline-none focus-visible:ring-2 focus-visible:ring-[#7E9181] focus-visible:ring-offset-2 cursor-pointer"
                >
                    <div className="w-8 h-8 rounded-lg bg-[#2E3532] text-white flex items-center justify-center text-xs font-semibold shadow-xs">
                        {userInitials}
                    </div>

                    <div className="hidden sm:flex flex-col text-left">
            <span className="text-xs font-semibold text-[#2E3532] leading-tight">
              {user ? `${user.firstName} ${user.lastName}` : "Candidate"}
            </span>
                        <span className="text-[11px] text-[#64748B] leading-tight truncate max-w-[140px]">
              {user?.email ?? "candidate@nextround.dev"}
            </span>
                    </div>

                    <ChevronDown
                        className={`w-3.5 h-3.5 text-[#64748B] transition-transform duration-200 ${
                            isDropdownOpen ? "rotate-180" : ""
                        }`}
                    />
                </button>

                {isDropdownOpen && (
                    <div
                        role="menu"
                        aria-orientation="vertical"
                        className="absolute right-0 mt-2 w-56 rounded-[10px] bg-white border border-[#E2E8F0] shadow-md py-1.5 z-50 text-xs animate-in fade-in-0 zoom-in-95 duration-100"
                    >
                        <div className="px-3 py-2 border-b border-[#E2E8F0] mb-1">
                            <p className="font-semibold text-[#2E3532]">Signed in as</p>
                            <p className="text-[11px] text-[#64748B] truncate mt-0.5">
                                {user?.email ?? "candidate@nextround.dev"}
                            </p>
                        </div>

                        <Link
                            to="/profile"
                            role="menuitem"
                            onClick={() => setIsDropdownOpen(false)}
                            className="flex items-center gap-2.5 px-3 py-2 text-[#2E3532] hover:bg-[#F1F5F3] transition-colors outline-none focus-visible:bg-[#F1F5F3] font-medium"
                        >
                            <User className="w-4 h-4 text-[#7E9181]" />
                            <span>Profile</span>
                        </Link>

                        <div className="my-1 border-t border-[#E2E8F0]" />

                        <button
                            type="button"
                            role="menuitem"
                            onClick={handleSignOutPlaceholder}
                            className="w-full flex items-center gap-2.5 px-3 py-2 text-[#991B1B] hover:bg-rose-50 transition-colors outline-none focus-visible:bg-rose-50 font-medium text-left cursor-pointer"
                        >
                            <LogOut className="w-4 h-4 text-[#991B1B]" />
                            <span>Sign Out</span>
                        </button>
                    </div>
                )}
            </div>
        </header>
    );
}

export default AppHeader;