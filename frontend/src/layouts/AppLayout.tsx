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
            {/* Sidebar - full-height container */}
            <aside className="w-64 border-r border-slate-200 bg-white flex flex-col shrink-0">
                <div className="p-4 border-b border-slate-100">
                    <span className="text-xl font-bold tracking-tight text-blue-600">NextRound</span>
                </div>
                <nav className="p-2 space-y-1">
                    {navLinks.map((link) => (
                        <NavLink
                            key={link.to}
                            to={link.to}
                            className={({ isActive }) =>
                                `block px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                                    isActive
                                        ? "bg-blue-50 text-blue-600 font-semibold"
                                        : "text-slate-600 hover:bg-slate-100 hover:text-slate-900"
                                }`
                            }
                        >
                            {link.name}
                        </NavLink>
                    ))}
                </nav>
            </aside>

            {/* Content Area - handles horizontal/vertical overflow for the board */}
            <main className="min-w-0 flex-1 overflow-y-auto p-6">
                <Outlet />
            </main>
        </div>
    );
}