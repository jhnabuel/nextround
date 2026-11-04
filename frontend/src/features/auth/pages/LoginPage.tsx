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
    const [errorMessage, setErrorMessage] = useState<string | null>(null);

    const from =
        (location.state as { from?: { pathname: string } })?.from?.pathname ||
        "/applications";

    const {
        register,
        handleSubmit,
        formState: { errors, isSubmitting },
    } = useForm<LoginRequest>();

    const onSubmit = async (data: LoginRequest) => {
        setErrorMessage(null);

        try {
            await login(data);
            toast.success("Welcome back! Signed in successfully.");
            navigate(from, { replace: true });
        } catch (err: any) {
            const status = err.response?.status;
            const serverMsg = err.response?.data?.message || err.response?.data?.error;

            if (
                status === 401 ||
                status === 403 ||
                (serverMsg && /authentication/i.test(serverMsg))
            ) {
                setErrorMessage("Invalid email or password. Please check your credentials and try again.");
            } else if (serverMsg && typeof serverMsg === "string") {
                setErrorMessage(serverMsg);
            } else {
                setErrorMessage("Unable to sign in. Please verify your connection or try again later.");
            }
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

            {errorMessage && (
                <div className="mb-5 flex items-center gap-2.5 rounded-lg border border-rose-200 bg-rose-50 px-3.5 py-2.5 text-xs text-rose-700 animate-in fade-in-0 duration-150">
                    <AlertCircle className="h-4 w-4 shrink-0 text-rose-600" />
                    <span className="font-medium">{errorMessage}</span>
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
                        aria-invalid={!!errors.email || !!errorMessage}
                        aria-describedby={errors.email ? "email-error" : undefined}
                        {...register("email", {
                            required: "Email is required",
                            pattern: {
                                value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                                message: "Enter a valid email address",
                            },
                            onChange: () => setErrorMessage(null),
                        })}
                        className={inputClass(!!errors.email || !!errorMessage)}
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
                        aria-invalid={!!errors.password || !!errorMessage}
                        aria-describedby={errors.password ? "password-error" : undefined}
                        {...register("password", {
                            required: "Password is required",
                            onChange: () => setErrorMessage(null),
                        })}
                        className={inputClass(!!errors.password || !!errorMessage)}
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
                    className="flex h-11 w-full items-center justify-center gap-2 rounded-lg bg-[#2E3532] px-4 text-sm font-medium text-white transition-colors hover:bg-[#3D4843] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#7E9181] focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-60 cursor-pointer"
                >
                    {isSubmitting ? (
                        <>
                            <Loader2 className="h-4 w-4 animate-spin text-[#7E9181]" />
                            <span>Signing in...</span>
                        </>
                    ) : (
                        <>
                            <span>Sign in</span>
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