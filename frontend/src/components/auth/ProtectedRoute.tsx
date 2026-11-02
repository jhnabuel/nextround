import { Navigate, Outlet, useLocation } from "react-router-dom";
import { Loader2 } from "lucide-react";
import { useAuth } from "../../context/AuthContext";

interface ProtectedRouteProps {
    children?: React.ReactNode;
}

export function ProtectedRoute({ children }: ProtectedRouteProps) {

    const { isAuthenticated, isLoading } = useAuth();
    const location = useLocation();

    // 1. Hold redirect while initial /users/me verification is pending
    if (isLoading) {
        return (
            <div className="flex h-screen w-screen flex-col items-center justify-center bg-[#F8FAFC]">
                <div className="flex flex-col items-center gap-3">
                    <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-[#2E3532] shadow-sm">
                        <Loader2 className="h-5 w-5 animate-spin text-[#7E9181]" />
                    </div>
                    <p className="text-xs font-medium text-[#64748B]">
                        Verifying session...
                    </p>
                </div>
            </div>
        );
    }

    // 2. Redirect to /login if unauthenticated, saving attempted URL
    if (!isAuthenticated) {
        return <Navigate to="/login" state={{ from: location }} replace />;
    }

    // 3. Render nested child routes or passed children
    return children ? <>{children}</> : <Outlet />;
}

export default ProtectedRoute;