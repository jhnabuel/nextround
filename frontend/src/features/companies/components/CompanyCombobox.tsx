import { useState, useRef, useEffect } from "react";
import { Check, ChevronsUpDown, Plus, Loader2 } from "lucide-react";
import { useCompanies } from "../api/useCompanies";

interface CompanyComboboxProps {
    selectedCompanyId?: string;
    selectedCompanyName?: string;
    onSelect: (selection: { companyId?: string; companyName?: string }) => void;
    error?: boolean;
}


export function CompanyCombobox({
    selectedCompanyId,
    selectedCompanyName,
    onSelect,
    error = false, }: CompanyComboboxProps){

    const [isOpen, setIsOpen] = useState(false);
    const [searchTerm, setSearchTerm] = useState("");
    const dropdownRef = useRef<HTMLDivElement>(null);
    const { data: companies = [], isLoading } = useCompanies(searchTerm);

    // Close dropdown on outside click
    useEffect(() => {
        function handleClickOutside(event: MouseEvent){
            if(dropdownRef.current && !dropdownRef.current.contains(event.target as Node)){
                setIsOpen(false);
            }
        }
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    // Sync display with pre-selected item
    useEffect(() => {
        if(selectedCompanyName){
            setSearchTerm(selectedCompanyName);
        }
    }, [selectedCompanyName]);

    const trimmed = searchTerm.trim();

    const exactMatch = companies.some((c) => c.companyName.toLowerCase() === trimmed.toLowerCase());

    const handleSelectExisting = (id: string, name: string) =>{
        onSelect({companyId: id, companyName: name});
        setSearchTerm(name);
        setIsOpen(false);
    };

    const handleCreateNew = () => {
        if (!trimmed) return;
        onSelect({ companyId: undefined, companyName: trimmed });
        setIsOpen(false);
    };

    return(<div ref={dropdownRef} className="relative w-full">
        <div className="relative">
            <input
                type="text"
                value={searchTerm}
                onChange={(e) => {
                    setSearchTerm(e.target.value);
                    onSelect({ companyId: undefined, companyName: e.target.value });
                    if (!isOpen) setIsOpen(true);
                }}
                onFocus={() => setIsOpen(true)}
                placeholder="Search or enter company..."
                className={`h-10 w-full rounded-lg border bg-white pl-3 pr-9 text-xs text-[#2E3532] placeholder:text-[#94A3B8] outline-none transition-all ${
                    error
                        ? "border-rose-400 focus:ring-1 focus:ring-rose-200"
                        : "border-[#E2E8F0] hover:border-[#CBD5E1] focus:border-[#7E9181] focus:ring-1 focus:ring-[#7E9181]"
                }`}
            />
            <button
                type="button"
                onClick={() => setIsOpen((prev) => !prev)}
                className="absolute right-2.5 top-1/2 -translate-y-1/2 text-[#94A3B8] hover:text-[#2E3532]"
            >
                {isLoading ? (
                    <Loader2 className="h-4 w-4 animate-spin text-[#7E9181]" />
                ) : (
                    <ChevronsUpDown className="h-4 w-4" />
                )}
            </button>
        </div>

        {isOpen && (
            <div className="absolute z-50 mt-1 max-h-56 w-full overflow-auto rounded-lg border border-[#E2E8F0] bg-white p-1 shadow-lg animate-in fade-in-0 duration-100">
                {companies.length > 0 && (
                    <div className="space-y-0.5">
                        {companies.map((c) => {
                            const isSelected = selectedCompanyId === c.id;
                            return (
                                <button
                                    key={c.id}
                                    type="button"
                                    onClick={() => handleSelectExisting(c.id, c.companyName)}
                                    className={`flex w-full items-center justify-between rounded-md px-2.5 py-1.5 text-left text-xs transition-colors ${
                                        isSelected
                                            ? "bg-[#7E9181]/15 text-[#2E3532] font-semibold"
                                            : "text-[#2E3532] hover:bg-[#F8FAFC]"
                                    }`}
                                >
                    <span>
                      {c.companyName}{" "}
                        {c.location && (
                            <span className="text-[11px] text-[#64748B]">({c.location})</span>
                        )}
                    </span>
                                    {isSelected && <Check className="h-3.5 w-3.5 text-[#7E9181]" />}
                                </button>
                            );
                        })}
                    </div>
                )}

                {/* Inline Find-or-Create Option */}
                {trimmed && !exactMatch && (
                    <button
                        type="button"
                        onClick={handleCreateNew}
                        className="flex w-full items-center gap-1.5 rounded-md border-t border-[#F1F5F9] px-2.5 py-2 text-left text-xs text-[#2E3532] hover:bg-[#F8FAFC]"
                    >
                        <Plus className="h-3.5 w-3.5 text-[#7E9181]" />
                        <span>
                Create new company <strong className="font-semibold">"{trimmed}"</strong>
              </span>
                    </button>
                )}

                {!isLoading && companies.length === 0 && !trimmed && (
                    <div className="py-2.5 text-center text-xs text-[#64748B]">
                        Type a company name to search or create
                    </div>
                )}
            </div>
        )}
    </div>);
}