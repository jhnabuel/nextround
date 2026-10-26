import { createBrowserRouter, Navigate } from "react-router-dom";
import AppLayout from "../layouts/AppLayout";
import AuthLayout from "../layouts/AuthLayout";
import  DashboardPage  from "../pages/DashboardPage";
import  NotFoundPage  from "../pages/NotFoundPage";
import  ApplicationsPage  from "../pages/ApplicationsPage";
import  ProfilePage  from "../pages/ProfilePage";
import  LoginPage  from "../pages/LoginPage";
import  RegisterPage  from "../pages/RegisterPage";

export const router = createBrowserRouter([
    {
        path: "/",
        element: <Navigate to="/applications" replace />,
    },
    {
        element: <AppLayout />,
        children: [
            { path: "dashboard", element: <DashboardPage /> },
            { path: "applications", element: <ApplicationsPage /> },
            { path: "profile", element: <ProfilePage /> },
        ],
    },
    {
        element: <AuthLayout />,
        children: [
            { path: "login", element: <LoginPage /> },
            { path: "register", element: <RegisterPage /> },
        ],
    },
    {
        path: "*",
        element: <NotFoundPage />,
    },
]);