import { Navigate, Outlet, useLocation } from "react-router-dom";
import { Loader2 } from "lucide-react";
import { useAuth } from "../../context/useAuth";

interface PublicRouteProps {
    children?: React.ReactNode;
}

export function PublicRoute({ children }: PublicRouteProps) {
    const { isAuthenticated, isLoading } = useAuth();
    const location = useLocation();

    // 1. Hold redirect while initial session check is pending
    if (isLoading) {
        return (
            <div className="flex h-screen w-screen flex-col items-center justify-center bg-[#F8FAFC]">
                <div className="flex flex-col items-center gap-3">
                    <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-[#2E3532] shadow-xs">
                        <Loader2 className="h-5 w-5 animate-spin text-[#7E9181]" />
                    </div>
                    <p className="text-xs font-medium text-[#64748B]">Loading...</p>
                </div>
            </div>
        );
    }

    // 2. If logged in, redirect away to target origin or default dashboard
    if (isAuthenticated) {
        const origin =
            (location.state as { from?: { pathname: string } })?.from?.pathname ||
            "/applications";
        return <Navigate to={origin} replace />;
    }

    // 3. Render public auth routes
    return children ? <>{children}</> : <Outlet />;
}

export default PublicRoute;