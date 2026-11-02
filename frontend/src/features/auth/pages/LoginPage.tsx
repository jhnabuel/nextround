import { useState } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { useForm } from "react-hook-form";
import { Loader2, Mail, Lock, AlertCircle } from "lucide-react";
import { useAuth } from "../../../context/useAuth";
import type { LoginRequest } from "../types/auth.types";
import axios from "axios";

export function LoginPage() {
    const { login } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();
    const [serverError, setServerError] = useState<string | null>(null);

    const from =
        (location.state as { from?: { pathname: string } })?.from?.pathname ||
        "/applications";

    const {
        register,
        handleSubmit,
        formState: { errors, isSubmitting },
    } = useForm<LoginRequest>({
        defaultValues: {
            email: "",
            password: "",
        },
    });

    const onSubmit = async (data: LoginRequest) => {
        setServerError(null);
        try {
            await login(data);
            navigate(from, { replace: true });
        } catch (err: unknown) {
            if (axios.isAxiosError(err) && err.response) {
                setServerError(
                    err.response.data?.message || "Invalid email or password."
                );
            } else {
                setServerError("Failed to sign in. Please try again.");
            }
        }
    };

    return (
        <div className="w-full max-w-md mx-auto">
            <div className="text-center mb-8">
                <h1 className="text-2xl font-bold tracking-tight text-[#2E3532]">
                    Welcome back
                </h1>
                <p className="text-xs text-[#64748B] mt-1.5">
                    Enter your credentials to access your job pipeline
                </p>
            </div>

            {serverError && (
                <div className="mb-5 p-3 rounded-lg bg-rose-50 border border-rose-200 text-rose-700 text-xs flex items-center gap-2">
                    <AlertCircle className="w-4 h-4 shrink-0" />
                    <span>{serverError}</span>
                </div>
            )}

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                <div>
                    <label className="block text-xs font-semibold text-[#2E3532] mb-1.5">
                        Email address
                    </label>
                    <div className="relative">
                        <Mail className="w-4 h-4 text-[#64748B] absolute left-3 top-1/2 -translate-y-1/2" />
                        <input
                            type="email"
                            autoComplete="email"
                            {...register("email", {
                                required: "Email is required",
                                pattern: {
                                    value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                                    message: "Invalid email address",
                                },
                            })}
                            placeholder="candidate@nextround.dev"
                            className={`w-full pl-9 pr-3 py-2 text-xs rounded-lg border bg-white text-[#2E3532] placeholder:text-[#94A3B8] outline-none transition-colors ${
                                errors.email
                                    ? "border-rose-400 focus:ring-2 focus:ring-rose-200"
                                    : "border-[#E2E8F0] focus:border-[#7E9181] focus:ring-2 focus:ring-[#7E9181]/20"
                            }`}
                        />
                    </div>
                    {errors.email && (
                        <p className="text-[11px] text-rose-600 mt-1">
                            {errors.email.message}
                        </p>
                    )}
                </div>

                <div>
                    <div className="flex items-center justify-between mb-1.5">
                        <label className="text-xs font-semibold text-[#2E3532]">
                            Password
                        </label>
                    </div>
                    <div className="relative">
                        <Lock className="w-4 h-4 text-[#64748B] absolute left-3 top-1/2 -translate-y-1/2" />
                        <input
                            type="password"
                            autoComplete="current-password"
                            {...register("password", {
                                required: "Password is required",
                            })}
                            placeholder="••••••••"
                            className={`w-full pl-9 pr-3 py-2 text-xs rounded-lg border bg-white text-[#2E3532] placeholder:text-[#94A3B8] outline-none transition-colors ${
                                errors.password
                                    ? "border-rose-400 focus:ring-2 focus:ring-rose-200"
                                    : "border-[#E2E8F0] focus:border-[#7E9181] focus:ring-2 focus:ring-[#7E9181]/20"
                            }`}
                        />
                    </div>
                    {errors.password && (
                        <p className="text-[11px] text-rose-600 mt-1">
                            {errors.password.message}
                        </p>
                    )}
                </div>

                <button
                    type="submit"
                    disabled={isSubmitting}
                    className="w-full mt-2 flex items-center justify-center gap-2 py-2 px-4 rounded-lg bg-[#2E3532] text-white text-xs font-semibold hover:bg-[#2E3532]/90 disabled:opacity-60 transition-colors cursor-pointer"
                >
                    {isSubmitting ? (
                        <>
                            <Loader2 className="w-3.5 h-3.5 animate-spin text-[#7E9181]" />
                            <span>Signing in...</span>
                        </>
                    ) : (
                        "Sign In"
                    )}
                </button>
            </form>

            <p className="text-center text-xs text-[#64748B] mt-6">
                Don't have an account?{" "}
                <Link
                    to="/register"
                    className="font-semibold text-[#2E3532] hover:text-[#7E9181] underline underline-offset-2"
                >
                    Create an account
                </Link>
            </p>
        </div>
    );
}

export default LoginPage;