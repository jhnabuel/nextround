import { createBrowserRouter, Navigate } from "react-router-dom";
import AppLayout from "../components/layout/AppLayout";
import AuthLayout from "../layouts/AuthLayout";
import ProtectedRoute from "../components/auth/ProtectedRoute";
import PublicRoute from "../components/auth/PublicRoute";

import DashboardPage from "../pages/DashboardPage";
import NotFoundPage from "../pages/NotFoundPage";
import ApplicationsPage from "../features/applications/pages/ApplicationsPage";
import ProfilePage from "../pages/ProfilePage";
import LoginPage from "../features/auth/pages/LoginPage";
import RegisterPage from "../features/auth/pages/RegisterPage";

export const router = createBrowserRouter([
    {
        path: "/",
        element: <Navigate to="/applications" replace />,
    },
    {
        element: <ProtectedRoute />,
        children: [
            {
                element: <AppLayout />,
                children: [
                    { path: "dashboard", element: <DashboardPage /> },
                    { path: "applications", element: <ApplicationsPage /> },
                    { path: "profile", element: <ProfilePage /> },
                ],
            },
        ],
    },
    {
        element: <PublicRoute/>,
        children: [
            {
                element: <AuthLayout />,
                children: [
                    { path: "login", element: <LoginPage /> },
                    { path: "register", element: <RegisterPage /> },
                ],
            }
        ],
    },
    {
        path: "*",
        element: <NotFoundPage />,
    },
]);