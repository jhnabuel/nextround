import { NavLink } from "react-router-dom";
import {
    LayoutDashboard,
    KanbanSquare,
    Building2,
    User,
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

export default function AppSidebar(){
    return(<>
        {/* Sidebar - full-height container */}
        <aside className="w-64 border-r border-slate-200 bg-white flex flex-col shrink-0 h-full">
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

    </>);
}