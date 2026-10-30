import type { ReactNode } from "react";

interface PageContainerProps {
    children: ReactNode;
    /** Full viewport width (useful for Kanban horizontal scrolling) vs bounded max-width */
    fluid?: boolean;
    className?: string;
}

interface PageHeaderProps {
    title: string;
    description?: string;
    badge?: ReactNode;
    actions?: ReactNode;
}

export function PageContainer({
                                  children,
                                  fluid = false,
                                  className = "",
                              }: PageContainerProps) {
    return (
        <div
            className={`flex flex-col h-full min-h-0 w-full ${
                fluid ? "max-w-none" : "max-w-7xl mx-auto"
            } ${className}`}
        >
            {children}
        </div>
    );
}

export function PageHeader({
                               title,
                               description,
                               badge,
                               actions,
                           }: PageHeaderProps) {
    return (
        <header className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between pb-6 shrink-0">
            <div>
                <div className="flex flex-wrap items-center gap-2.5">
                    <h1 className="text-2xl font-semibold tracking-tight text-[#1E293B]">
                        {title}
                    </h1>
                    {badge}
                </div>
                {description && (
                    <p className="mt-1 text-sm text-[#64748B]">{description}</p>
                )}
            </div>

            {actions && (
                <div className="flex items-center gap-2.5 shrink-0">{actions}</div>
            )}
        </header>
    );
}