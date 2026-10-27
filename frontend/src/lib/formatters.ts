/**
 * Formats a salary range dynamically using Intl.NumberFormat.
 * Supports compact notation (e.g., ₱60k, $120k) or standard currency display.
 */
export function formatSalaryRange(
    min?: number,
    max?: number,
    currency: string = "USD",
    locale: string = navigator.language || "en-US"
): string | null {
    if (!min && !max) return null;

    const formatter = new Intl.NumberFormat(locale, {
        style: "currency",
        currency: currency.toUpperCase(),
        notation: "compact",
        maximumFractionDigits: 1,
    });

    if (min && max) {
        if (min === max) {
            return formatter.format(min);
        }
        return `${formatter.format(min)} – ${formatter.format(max)}`;
    }

    if (min) return `${formatter.format(min)}+`;
    if (max) return `Up to ${formatter.format(max)}`;

    return null;
}

/**
 * Formats an ISO date string according to the user's local settings.
 * Defaults to navigator.language (e.g., "Oct 24" or "24 Oct" depending on region).
 */
export function formatDate(
    isoDateString?: string,
    locale: string = navigator.language || "en-US",
    options: Intl.DateTimeFormatOptions = { month: "short", day: "numeric" }
): string | null {
    if (!isoDateString) return null;

    try {
        const date = new Date(isoDateString);
        if (isNaN(date.getTime())) return null;
        return new Intl.DateTimeFormat(locale, options).format(date);
    } catch {
        return null;
    }
}