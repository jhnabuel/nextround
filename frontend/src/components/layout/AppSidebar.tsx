import type React from "react";
import { NavLink } from "react-router-dom";
import {
    LayoutDashboard,
    KanbanSquare,
    Building2,
    User,
    Layers,
    X,
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

interface AppSidebarProps {
    isMobileOpen: boolean;
    onCloseMobile: () => void;
}

export function AppSidebar({ isMobileOpen, onCloseMobile }: AppSidebarProps) {
    const renderNavLinks = (isMobile = false) => (
        <nav className="p-3 space-y-1 flex-1 overflow-y-auto" aria-label="Main navigation">
            {!isMobile && (
                <div className="hidden lg:block px-3 py-1.5 text-[11px] font-semibold uppercase tracking-wider text-[#94A3B8]">
                    Workspace
                </div>
            )}

            {navLinks.map((link) => {
                const Icon = link.icon;
                return (
                    <NavLink
                        key={link.to}
                        to={link.to}
                        onClick={() => {
                            if (isMobile) onCloseMobile();
                        }}
                        title={link.name}
                        className={({ isActive }) =>
                            `group relative flex items-center gap-3 rounded-[8px] text-[13px] font-medium transition-all duration-150 outline-none focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#7E9181] focus-visible:ring-offset-2 ${
                                isMobile
                                    ? "px-3 py-2.5"
                                    : "p-2.5 md:justify-center lg:justify-start lg:px-3 lg:py-2.5"
                            } ${
                                isActive
                                    ? "bg-[#7E9181]/15 text-[#2E3532] font-semibold"
                                    : "text-[#64748B] hover:bg-[#F8FAFC] hover:text-[#2E3532]"
                            }`
                        }
                    >
                        {({ isActive }) => (
                            <>
                                {isActive && (
                                    <span
                                        aria-hidden="true"
                                        className="absolute left-0 top-1.5 bottom-1.5 w-1 rounded-r-full bg-[#2E3532]"
                                    />
                                )}

                                <Icon
                                    className={`w-5 h-5 shrink-0 transition-colors ${
                                        isActive
                                            ? "text-[#2E3532]"
                                            : "text-[#7E9181] group-hover:text-[#2E3532]"
                                    }`}
                                />
                                <span className={isMobile ? "inline" : "inline md:hidden lg:inline"}>
                  {link.name}
                </span>
                            </>
                        )}
                    </NavLink>
                );
            })}
        </nav>
    );

    return (
        <>
            {/* =========================================================================
          1. Desktop (Expanded) & Tablet (Compact Rail)
          ========================================================================= */}
            <aside
                aria-label="Sidebar navigation"
                className="hidden md:flex flex-col h-full shrink-0 border-r border-[#E2E8F0] bg-white select-none transition-all duration-200 md:w-[68px] lg:w-60"
            >
                {/* Branding */}
                <div className="h-16 border-b border-[#E2E8F0] flex items-center md:justify-center lg:justify-start lg:px-5 shrink-0 gap-3">
                    <div className="w-8 h-8 rounded-lg bg-[#2E3532] text-white flex items-center justify-center shadow-xs shrink-0">
                        <Layers className="w-4 h-4 text-[#7E9181]" />
                    </div>
                    <div className="hidden lg:flex flex-col min-w-0">
            <span className="text-[15px] font-bold tracking-tight text-[#2E3532] leading-none">
              NextRound
            </span>
                        <span className="text-[10px] font-medium tracking-wide text-[#7E9181] uppercase mt-1">
              Job Tracker
            </span>
                    </div>
                </div>

                {/* Navigation Items */}
                {renderNavLinks(false)}

                {/* Footer Status */}
                <div className="p-3 border-t border-[#E2E8F0] shrink-0">
                    <div className="flex items-center md:justify-center lg:justify-start gap-2 p-2 lg:px-3 lg:py-2 rounded-[8px] bg-[#F8FAFC] text-[11px] text-[#64748B]">
                        <span className="w-2 h-2 rounded-full bg-[#7E9181] shrink-0" />
                        <span className="font-medium text-[#2E3532] hidden lg:inline">
              Pipeline Active
            </span>
                    </div>
                </div>
            </aside>

            {/* =========================================================================
          2. Mobile Slide-Over Drawer
          ========================================================================= */}
            {isMobileOpen && (
                <div className="fixed inset-0 z-50 md:hidden flex" role="dialog" aria-modal="true">
                    {/* Backdrop */}
                    <div
                        className="fixed inset-0 bg-slate-900/40 backdrop-blur-xs transition-opacity animate-in fade-in"
                        onClick={onCloseMobile}
                        aria-hidden="true"
                    />

                    {/* Drawer Content */}
                    <div className="relative w-72 max-w-[80vw] bg-white h-full flex flex-col shadow-xl z-10 animate-in slide-in-from-left duration-200">
                        <div className="h-16 px-5 border-b border-[#E2E8F0] flex items-center justify-between shrink-0">
                            <div className="flex items-center gap-3">
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

                            <button
                                type="button"
                                onClick={onCloseMobile}
                                className="p-1.5 rounded-lg text-[#64748B] hover:bg-[#F1F5F3] hover:text-[#2E3532] outline-none focus-visible:ring-2 focus-visible:ring-[#7E9181]"
                                aria-label="Close menu"
                            >
                                <X className="w-5 h-5" />
                            </button>
                        </div>

                        {renderNavLinks(true)}

                        <div className="p-4 border-t border-[#E2E8F0]">
                            <div className="flex items-center gap-2 px-3 py-2 rounded-[8px] bg-[#F8FAFC] text-xs text-[#64748B]">
                                <span className="w-2 h-2 rounded-full bg-[#7E9181]" />
                                <span className="font-medium text-[#2E3532]">Pipeline Active</span>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </>
    );
}

export default AppSidebar;