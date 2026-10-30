import { Outlet, NavLink } from "react-router-dom";

const navLinks = [
    { name: "Dashboard", to: "/dashboard" },
    { name: "Applications", to: "/applications" },
    { name: "Companies", to: "/companies" },
    { name: "Profile", to: "/profile" },
];

export default function AppLayout() {
    return (
        <div className="flex h-screen overflow-hidden bg-slate-50 text-slate-900">
            {/* Content Area - handles horizontal/vertical overflow for the board */}
            <main className="min-w-0 flex-1 p-6 overflow-auto">
                <Outlet />
            </main>
        </div>
    );
}