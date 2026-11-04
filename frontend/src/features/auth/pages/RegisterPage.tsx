
import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { toast } from "sonner";
import { Loader2, AlertCircle, ArrowRight } from "lucide-react";
import { useAuth } from "../../../context/useAuth";
import type { RegisterRequest } from "../types/auth.types";

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
            navigate("/application", { replace: true });
        } catch (err: any) {
            const message = err.response?.data?.message || err.message || "Registration failed. Please check your details and try again.";
            setServerError(message);
            toast.error(message);
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
                            aria-invalid={!!errors.firstName}
                            {...register("firstName", {
                                required: "First name is required",
                            })}
                            className={inputClass(!!errors.firstName)}
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
                            aria-invalid={!!errors.lastName}
                            {...register("lastName", {
                                required: "Last name is required",
                            })}
                            className={inputClass(!!errors.lastName)}
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
                        aria-invalid={!!errors.email}
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
                        aria-invalid={!!errors.password}
                        {...register("password", {
                            required: "Password is required",
                            minLength: {
                                value: 8,
                                message: "Password must be at least 8 characters",
                            },
                        })}
                        className={inputClass(!!errors.password)}
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
