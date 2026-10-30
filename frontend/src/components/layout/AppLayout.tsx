import { Outlet, NavLink } from "react-router-dom";
import AppSidebar from "./AppSidebar"
import AppHeader from "./AppHeader"

const navLinks = [
    { name: "Dashboard", to: "/dashboard" },
    { name: "Applications", to: "/applications" },
    { name: "Companies", to: "/companies" },
    { name: "Profile", to: "/profile" },
];

export default function AppLayout() {
    return (
        <div className="flex h-screen w-screen overflow-hidden bg-[#F8FAFC] text-[#1E293B]">
            {/* 1. Left Sidebar */}
            <AppSidebar />

            {/* 2. Right Main Layout Column */}
            <div className="flex flex-col flex-1 min-w-0 h-full overflow-hidden">
                {/* Top Header */}
                <AppHeader />

                {/* Routed Page Viewport Container */}
                <main className="flex-1 min-h-0 min-w-0 p-6 overflow-y-auto">
                    <Outlet />
                </main>
            </div>
        </div>
    );
}