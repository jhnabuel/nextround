import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { Loader2, Mail, Lock, User, AlertCircle } from "lucide-react";
import { useAuth } from "../../../context/useAuth";
import type { RegisterRequest } from "../types/auth.types";
import axios from "axios";

export function RegisterPage() {
    const { register: registerUser } = useAuth();
    const navigate = useNavigate();
    const [serverError, setServerError] = useState<string | null>(null);

    const {
        register,
        handleSubmit,
        formState: { errors, isSubmitting },
    } = useForm<RegisterRequest>({
        defaultValues: {
            firstName: "",
            lastName: "",
            email: "",
            password: "",
        },
    });

    const onSubmit = async (data: RegisterRequest) => {
        setServerError(null);
        try {
            await registerUser(data);
            navigate("/applications", { replace: true });
        } catch (err: unknown) {
            if (axios.isAxiosError(err) && err.response) {
                setServerError(
                    err.response.data?.message || "Registration failed. Please check your inputs."
                );
            } else {
                setServerError("Could not complete registration. Please try again.");
            }
        }
    };

    return (
        <div className="w-full max-w-md mx-auto">
            <div className="text-center mb-8">
                <h1 className="text-2xl font-bold tracking-tight text-[#2E3532]">
                    Create your account
                </h1>
                <p className="text-xs text-[#64748B] mt-1.5">
                    Start tracking your job search and interviews in NextRound
                </p>
            </div>

            {serverError && (
                <div className="mb-5 p-3 rounded-lg bg-rose-50 border border-rose-200 text-rose-700 text-xs flex items-center gap-2">
                    <AlertCircle className="w-4 h-4 shrink-0" />
                    <span>{serverError}</span>
                </div>
            )}

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                <div className="grid grid-cols-2 gap-3">
                    <div>
                        <label className="block text-xs font-semibold text-[#2E3532] mb-1.5">
                            First name
                        </label>
                        <div className="relative">
                            <User className="w-4 h-4 text-[#64748B] absolute left-3 top-1/2 -translate-y-1/2" />
                            <input
                                type="text"
                                {...register("firstName", { required: "First name is required" })}
                                placeholder="John"
                                className={`w-full pl-9 pr-3 py-2 text-xs rounded-lg border bg-white text-[#2E3532] placeholder:text-[#94A3B8] outline-none transition-colors ${
                                    errors.firstName
                                        ? "border-rose-400 focus:ring-2 focus:ring-rose-200"
                                        : "border-[#E2E8F0] focus:border-[#7E9181] focus:ring-2 focus:ring-[#7E9181]/20"
                                }`}
                            />
                        </div>
                        {errors.firstName && (
                            <p className="text-[11px] text-rose-600 mt-1">
                                {errors.firstName.message}
                            </p>
                        )}
                    </div>

                    <div>
                        <label className="block text-xs font-semibold text-[#2E3532] mb-1.5">
                            Last name
                        </label>
                        <input
                            type="text"
                            {...register("lastName", { required: "Last name is required" })}
                            placeholder="Abuel"
                            className={`w-full px-3 py-2 text-xs rounded-lg border bg-white text-[#2E3532] placeholder:text-[#94A3B8] outline-none transition-colors ${
                                errors.lastName
                                    ? "border-rose-400 focus:ring-2 focus:ring-rose-200"
                                    : "border-[#E2E8F0] focus:border-[#7E9181] focus:ring-2 focus:ring-[#7E9181]/20"
                            }`}
                        />
                        {errors.lastName && (
                            <p className="text-[11px] text-rose-600 mt-1">
                                {errors.lastName.message}
                            </p>
                        )}
                    </div>
                </div>

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
                    <label className="block text-xs font-semibold text-[#2E3532] mb-1.5">
                        Password
                    </label>
                    <div className="relative">
                        <Lock className="w-4 h-4 text-[#64748B] absolute left-3 top-1/2 -translate-y-1/2" />
                        <input
                            type="password"
                            autoComplete="new-password"
                            {...register("password", {
                                required: "Password is required",
                                minLength: {
                                    value: 8,
                                    message: "Password must be at least 8 characters",
                                },
                            })}
                            placeholder="Minimum 8 characters"
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
                            <span>Creating account...</span>
                        </>
                    ) : (
                        "Create Account"
                    )}
                </button>
            </form>

            <p className="text-center text-xs text-[#64748B] mt-6">
                Already have an account?{" "}
                <Link
                    to="/login"
                    className="font-semibold text-[#2E3532] hover:text-[#7E9181] underline underline-offset-2"
                >
                    Sign in
                </Link>
            </p>
        </div>
    );
}

export default RegisterPage;