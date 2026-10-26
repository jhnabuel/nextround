import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/useAuth';


export default function AuthLayout() {
    const { token, loading } = useAuth();

    // Prevent flicker while inspecting localStorage on page mount
    if (loading) {
        return (
            <div className="min-h-screen w-full flex items-center justify-center bg-slate-100">
                <div className="text-sm font-medium text-slate-500">Checking credentials...</div>
            </div>
        );
    }

    // Already authenticated -> redirect to Kanban board
    if (token) {
        return <Navigate to="/applications" replace />;
    }

    return(
        <div className="min-h-screen flex items-center justify-center bg-slate-100 p-4">
            <div className="w-full max-w-md bg-white p-8 rounded-xl shadow-sm border border-slate-200">
                <div className="text-center mb-6">
                    <h1 className="text-2xl font-black text-blue-600">NextRound</h1>
                    <p className="text-sm text-slate-500 mt-1">Application Pipeline Tracker</p>
                </div>
                <Outlet />
            </div>
        </div>
    );
};