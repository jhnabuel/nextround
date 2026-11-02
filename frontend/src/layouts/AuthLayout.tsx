
import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../context/useAuth";
import { Loader2 } from "lucide-react";

export default function AuthLayout() {
    const { token, loading } = useAuth();

    // Prevent authentication flicker during initialization
    if (loading) {
        return (
            <div className="min-h-screen w-full flex items-center justify-center bg-[#F8FAFC]">
                <div className="flex items-center gap-2 text-sm text-[#64748B]">
                    <Loader2 className="h-4 w-4 animate-spin text-[#7E9181]" />
                    Checking credentials...
                </div>
            </div>
        );
    }

    // Redirect authenticated users
    if (token) {
        return <Navigate to="/applications" replace />;
    }

    return (
        <div className="min-h-screen flex items-center justify-center bg-[#F8FAFC] px-4 py-10">
            <div className="w-full max-w-[420px]">

                {/* Brand */}
                <div className="text-center mb-8">
                    <div className="inline-flex items-center gap-2">
                        <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-[#2E3532] text-white text-lg font-bold">
                            N
                        </div>

                        <span className="text-xl font-semibold tracking-tight text-[#2E3532]">
                            NextRound
                        </span>
                    </div>

                    <p className="mt-2 text-sm text-[#64748B]">
                        Application Pipeline Tracker
                    </p>
                </div>

                {/* Authentication Card */}
                <div className="rounded-xl border border-[#E2E8F0] bg-white p-6 shadow-sm sm:p-8">
                    <Outlet />
                </div>

                {/* Footer */}
                <p className="mt-6 text-center text-xs text-[#94A3B8]">
                    Stay organized. Keep moving forward.
                </p>
            </div>
        </div>
    );
}
