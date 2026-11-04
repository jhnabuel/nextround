
import { useState } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { useForm } from "react-hook-form";
import { Loader2, AlertCircle, ArrowRight } from "lucide-react";
import { useAuth } from "../../../context/useAuth";
import type { LoginRequest } from "../types/auth.types";
import { toast } from "sonner";

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
    } = useForm<LoginRequest>();

    const onSubmit = async (data: LoginRequest) => {
        setServerError(null);

        try {
            await login(data);
            toast.success("Welcome back! Signed in successfully.");
            navigate(from, { replace: true });
        } catch (err: any) {
           let errorMessage = "Invalid email or password. Please try again";

           if(err.response?.status === 401 || err.response?.status === 400){
               errorMessage = err.response.data?.message || "Incorrect email or password.";
           } else if (err.response?.data?.message){
               errorMessage = err.response.data.message;
           }

           setServerError(errorMessage);
           toast.error(errorMessage);
        }
    };

    const inputClass = (hasError: boolean) =>
        `h-11 w-full rounded-lg border bg-white px-3.5 text-sm text-[#2E3532]
        placeholder:text-[#94A3B8] outline-none transition-all
        ${
            hasError
                ? "border-rose-400 focus:ring-2 focus:ring-rose-100"
                : "border-[#E2E8F0] hover:border-[#CBD5E1] focus:border-[#7E9181] focus:ring-2 focus:ring-[#7E9181]/15"
        }`;

    return (
        <>
            <div className="mb-6">
                <h1 className="text-2xl font-semibold tracking-tight text-[#2E3532]">
                    Welcome back
                </h1>

                <p className="mt-2 text-sm text-[#64748B]">
                    Sign in to continue managing your job search.
                </p>
            </div>

            {serverError && (
                <div
                    role="alert"
                    className="mb-5 flex items-start gap-2.5 rounded-lg border border-rose-200 bg-rose-50 p-3 text-sm text-rose-700"
                >
                    <AlertCircle className="mt-0.5 h-4 w-4 shrink-0" />
                    <span>{serverError}</span>
                </div>
            )}

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
                <div className="space-y-2">
                    <label htmlFor="email" className="text-sm font-medium text-[#2E3532]">
                        Email address
                    </label>

                    <input
                        id="email"
                        type="email"
                        autoComplete="email"
                        placeholder="you@example.com"
                        aria-invalid={!!errors.email}
                        aria-describedby={errors.email ? "email-error" : undefined}
                        {...register("email", {
                            required: "Email is required",
                            pattern: {
                                value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                                message: "Enter a valid email address",
                            },
                        })}
                        className={inputClass(!!errors.email)}
                    />

                    {errors.email && (
                        <p id="email-error" className="text-xs text-rose-600">
                            {errors.email.message}
                        </p>
                    )}
                </div>

                <div className="space-y-2">
                    <label htmlFor="password" className="text-sm font-medium text-[#2E3532]">
                        Password
                    </label>

                    <input
                        id="password"
                        type="password"
                        autoComplete="current-password"
                        placeholder="Enter your password"
                        aria-invalid={!!errors.password}
                        aria-describedby={errors.password ? "password-error" : undefined}
                        {...register("password", {
                            required: "Password is required",
                        })}
                        className={inputClass(!!errors.password)}
                    />

                    {errors.password && (
                        <p id="password-error" className="text-xs text-rose-600">
                            {errors.password.message}
                        </p>
                    )}
                </div>

                <button
                    type="submit"
                    disabled={isSubmitting}
                    className="flex h-11 w-full items-center justify-center gap-2 rounded-lg bg-[#2E3532] px-4 text-sm font-medium text-white transition-colors hover:bg-[#3D4843] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#7E9181] focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-60"
                >
                    {isSubmitting ? (
                        <>
                            <Loader2 className="h-4 w-4 animate-spin" />
                            Signing in...
                        </>
                    ) : (
                        <>
                            Sign in
                            <ArrowRight className="h-4 w-4" />
                        </>
                    )}
                </button>
            </form>

            <div className="mt-6 border-t border-[#E2E8F0] pt-6 text-center">
                <p className="text-sm text-[#64748B]">
                    Don't have an account?{" "}
                    <Link
                        to="/register"
                        className="font-semibold text-[#2E3532] underline-offset-4 hover:text-[#7E9181] hover:underline"
                    >
                        Create an account
                    </Link>
                </p>
            </div>
        </>
    );
}

export default LoginPage;
