import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { toast } from "sonner";
import { Loader2, AlertCircle, ArrowRight } from "lucide-react";
import { useAuth } from "../../../context/useAuth";
import type { RegisterRequest } from "../types/auth.types";
import { isAxiosError } from "axios";

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
            toast.success("Account created successfully! Welcome to NextRound.");
            navigate("/applications", { replace: true });
        } catch (err: unknown) {
            let status: number | undefined;
            let serverMsg: string | undefined;

            if (isAxiosError<{ message?: string; error?: string }>(err)) {
                status = err.response?.status;
                serverMsg = err.response?.data?.message || err.response?.data?.error;
            } else if (err instanceof Error) {
                serverMsg = err.message;
            }

            if (status === 409 || (serverMsg && /exists|duplicate|registered/i.test(serverMsg))) {
                setServerError("An account with this email address already exists.");
            } else if (serverMsg) {
                setServerError(serverMsg);
            } else {
                setServerError("Registration failed. Please check your details and try again.");
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
                    Create your account
                </h1>

                <p className="mt-2 text-sm text-[#64748B]">
                    Start organizing your applications and tracking your progress.
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
                <div className="grid grid-cols-1 gap-5 sm:grid-cols-2">
                    <div className="space-y-2">
                        <label htmlFor="firstName" className="text-sm font-medium text-[#2E3532]">
                            First name
                        </label>

                        <input
                            id="firstName"
                            type="text"
                            autoComplete="given-name"
                            placeholder="John"
                            aria-invalid={!!errors.firstName || !!serverError}
                            {...register("firstName", {
                                required: "First name is required",
                                onChange: () => setServerError(null),
                            })}
                            className={inputClass(!!errors.firstName || !!serverError)}
                        />

                        {errors.firstName && (
                            <p className="text-xs text-rose-600">
                                {errors.firstName.message}
                            </p>
                        )}
                    </div>

                    <div className="space-y-2">
                        <label htmlFor="lastName" className="text-sm font-medium text-[#2E3532]">
                            Last name
                        </label>

                        <input
                            id="lastName"
                            type="text"
                            autoComplete="family-name"
                            placeholder="Doe"
                            aria-invalid={!!errors.lastName || !!serverError}
                            {...register("lastName", {
                                required: "Last name is required",
                                onChange: () => setServerError(null),
                            })}
                            className={inputClass(!!errors.lastName || !!serverError)}
                        />

                        {errors.lastName && (
                            <p className="text-xs text-rose-600">
                                {errors.lastName.message}
                            </p>
                        )}
                    </div>
                </div>

                <div className="space-y-2">
                    <label htmlFor="email" className="text-sm font-medium text-[#2E3532]">
                        Email address
                    </label>

                    <input
                        id="email"
                        type="email"
                        autoComplete="email"
                        placeholder="you@example.com"
                        aria-invalid={!!errors.email || !!serverError}
                        {...register("email", {
                            required: "Email is required",
                            pattern: {
                                value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                                message: "Enter a valid email address",
                            },
                            onChange: () => setServerError(null),
                        })}
                        className={inputClass(!!errors.email || !!serverError)}
                    />

                    {errors.email && (
                        <p className="text-xs text-rose-600">
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
                        autoComplete="new-password"
                        placeholder="At least 8 characters"
                        aria-invalid={!!errors.password || !!serverError}
                        {...register("password", {
                            required: "Password is required",
                            minLength: {
                                value: 8,
                                message: "Password must be at least 8 characters",
                            },
                            onChange: () => setServerError(null),
                        })}
                        className={inputClass(!!errors.password || !!serverError)}
                    />

                    {errors.password && (
                        <p className="text-xs text-rose-600">
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
                            Creating account...
                        </>
                    ) : (
                        <>
                            Create account
                            <ArrowRight className="h-4 w-4" />
                        </>
                    )}
                </button>
            </form>

            <div className="mt-6 border-t border-[#E2E8F0] pt-6 text-center">
                <p className="text-sm text-[#64748B]">
                    Already have an account?{" "}
                    <Link
                        to="/login"
                        className="font-semibold text-[#2E3532] underline-offset-4 hover:text-[#7E9181] hover:underline"
                    >
                        Sign in
                    </Link>
                </p>
            </div>
        </>
    );
}

export default RegisterPage;