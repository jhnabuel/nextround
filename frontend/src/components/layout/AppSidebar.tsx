import type React from "react";
import { NavLink } from "react-router-dom";
import {
    LayoutDashboard,
    KanbanSquare,
    Building2,
    User,
    Layers,
} from "lucide-react";

interface NavItem {
    name: string;
    to: string;
    icon: React.ComponentType<{ className?: string }>;
}

const navLinks: NavItem[] = [
    { name: "Dashboard", to: "/dashboard", icon: LayoutDashboard },
    { name: "Applications", to: "/applications", icon: KanbanSquare },
    { name: "Companies", to: "/companies", icon: Building2 },
    { name: "Profile", to: "/profile", icon: User },
];

export function AppSidebar() {
    return (
        <aside
            aria-label="Sidebar navigation"
            className="w-[240px] shrink-0 border-r border-[#E2E8F0] bg-white flex flex-col h-full select-none"
        >
            {/* 1. NextRound Branding Section */}
            <div className="h-16 px-5 border-b border-[#E2E8F0] flex items-center gap-3 shrink-0">
                <div className="w-8 h-8 rounded-lg bg-[#2E3532] text-white flex items-center justify-center shadow-xs">
                    <Layers className="w-4 h-4 text-[#7E9181]" />
                </div>
                <div className="flex flex-col">
          <span className="text-[15px] font-bold tracking-tight text-[#2E3532] leading-none">
            NextRound
          </span>
                    <span className="text-[10px] font-medium tracking-wide text-[#7E9181] uppercase mt-1">
            Job Tracker
          </span>
                </div>
            </div>

            {/* 2. Navigation Items */}
            <nav className="p-3 space-y-1 flex-1 overflow-y-auto" aria-label="Main navigation">
                <div className="px-3 py-1.5 text-[11px] font-semibold uppercase tracking-wider text-[#94A3B8]">
                    Workspace
                </div>

                {navLinks.map((link) => {
                    const Icon = link.icon;
                    return (
                        <NavLink
                            key={link.to}
                            to={link.to}
                            className={({ isActive }) =>
                                `group relative flex items-center gap-3 px-3 py-2.5 rounded-[8px] text-[13px] font-medium transition-all duration-150 outline-none focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#7E9181] focus-visible:ring-offset-2 ${
                                    isActive
                                        ? "bg-[#7E9181]/15 text-[#2E3532] font-semibold"
                                        : "text-[#64748B] hover:bg-[#F8FAFC] hover:text-[#2E3532]"
                                }`
                            }
                        >
                            {({ isActive }) => (
                                <>
                                    {/* Subtle active left border accent */}
                                    {isActive && (
                                        <span
                                            aria-hidden="true"
                                            className="absolute left-0 top-1.5 bottom-1.5 w-1 rounded-r-full bg-[#2E3532]"
                                        />
                                    )}

                                    <Icon
                                        className={`w-4 h-4 shrink-0 transition-colors ${
                                            isActive
                                                ? "text-[#2E3532]"
                                                : "text-[#7E9181] group-hover:text-[#2E3532]"
                                        }`}
                                    />
                                    <span>{link.name}</span>
                                </>
                            )}
                        </NavLink>
                    );
                })}
            </nav>

            {/* 3. Footer / Environment Indicator */}
            <div className="p-3 border-t border-[#E2E8F0] shrink-0">
                <div className="flex items-center gap-2 px-3 py-2 rounded-[8px] bg-[#F8FAFC] text-[11px] text-[#64748B]">
                    <span className="w-2 h-2 rounded-full bg-[#7E9181]" />
                    <span className="font-medium text-[#2E3532]">Pipeline Active</span>
                </div>
            </div>
        </aside>
    );
}

export default AppSidebar;