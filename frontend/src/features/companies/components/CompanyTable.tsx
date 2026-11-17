import { ExternalLink, Pencil, Trash2 } from "lucide-react";
import type { CompanyResponse } from "../types/company.types";

interface CompanyTableProps {
    companies: CompanyResponse[];
    onEdit: (company: CompanyResponse) => void;
    onDelete: (company: CompanyResponse) => void;
}

function formatWebsiteLabel(url: string): string {
    return url.replace(/^https?:\/\/(www\.)?/, "").replace(/\/$/, "");
}

export function CompanyTable({ companies, onEdit, onDelete }: CompanyTableProps) {
    return (
        <div className="overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
            <div className="overflow-x-auto">
                <table className="w-full border-collapse text-left text-sm">
                    <thead className="border-b border-slate-200 bg-slate-50 text-xs font-semibold uppercase tracking-wider text-slate-600">
                    <tr>
                        <th className="px-4 py-3">Company</th>
                        <th className="px-4 py-3">Industry</th>
                        <th className="px-4 py-3">Location</th>
                        <th className="px-4 py-3 text-right">Actions</th>
                    </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-200">
                    {companies.map((company) => (
                        <tr
                            key={company.id}
                            className="transition-colors hover:bg-slate-50/80"
                        >
                            <td className="px-4 py-3.5">
                                <div className="font-medium text-slate-900">{company.companyName}</div>
                                {company.websiteUrl && (
                                    <a
                                        href={company.websiteUrl}
                                        target="_blank"
                                        rel="noopener noreferrer"
                                        className="mt-0.5 inline-flex items-center gap-1 text-xs text-slate-500 hover:text-slate-800 hover:underline"
                                    >
                                        <span>{formatWebsiteLabel(company.websiteUrl)}</span>
                                        <ExternalLink className="h-3 w-3" />
                                    </a>
                                )}
                            </td>

                            <td className="px-4 py-3.5 text-slate-600">
                                {company.industry || <span className="text-slate-400">—</span>}
                            </td>

                            <td className="px-4 py-3.5 text-slate-600">
                                {company.location || <span className="text-slate-400">—</span>}
                            </td>

                            <td className="px-4 py-3.5 text-right">
                                <div className="inline-flex items-center justify-end gap-1">
                                    <button
                                        type="button"
                                        onClick={() => onEdit(company)}
                                        aria-label={`Edit ${company.companyName}`}
                                        className="rounded-md p-1.5 text-slate-500 transition-colors hover:bg-slate-100 hover:text-slate-900"
                                    >
                                        <Pencil className="h-4 w-4" />
                                    </button>
                                    <button
                                        type="button"
                                        onClick={() => onDelete(company)}
                                        aria-label={`Delete ${company.companyName}`}
                                        className="rounded-md p-1.5 text-slate-500 transition-colors hover:bg-red-50 hover:text-red-600"
                                    >
                                        <Trash2 className="h-4 w-4" />
                                    </button>
                                </div>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}