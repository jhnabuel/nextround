import { useState } from "react";
import { Outlet } from "react-router-dom";
import AppSidebar from "./AppSidebar";
import AppHeader from "./AppHeader";

export default function AppLayout() {
    const [isMobileSidebarOpen, setIsMobileSidebarOpen] = useState(false);

    return (
        <div className="flex h-screen w-screen overflow-hidden bg-[#F8FAFC] text-[#1E293B]">
            {/* 1. Responsive Sidebar (Desktop Rail, Tablet Compact, Mobile Drawer) */}
            <AppSidebar
                isMobileOpen={isMobileSidebarOpen}
                onCloseMobile={() => setIsMobileSidebarOpen(false)}
            />

            {/* 2. Main Content Workspace */}
            <div className="flex flex-col flex-1 min-w-0 h-full overflow-hidden">
                {/* App Header with Mobile Hamburger Trigger */}
                <AppHeader onOpenMobileSidebar={() => setIsMobileSidebarOpen(true)} />

                {/* Viewport Container: keeps page-level horizontal overflow isolated to Kanban */}
                <main className="flex-1 min-h-0 min-w-0 p-4 sm:p-6 overflow-y-auto overflow-x-hidden">
                    <Outlet />
                </main>
            </div>
        </div>
    );
}