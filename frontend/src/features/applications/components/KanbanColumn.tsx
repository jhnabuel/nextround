import { useDroppable } from "@dnd-kit/core";
import type { ColumnConfig, KanbanApplication } from "../types/kanban.types";
import { ApplicationCard } from "./ApplicationCard";

interface KanbanColumnProps {
    column: ColumnConfig;
    applications: KanbanApplication[];
    onCardClick?: (id: string) => void;
}

export function KanbanColumn({ column, applications, onCardClick }: KanbanColumnProps) {
    const { setNodeRef, isOver } = useDroppable({
        id: column.id,
        data: { status: column.id },
    });

    return (
        <section
            ref={setNodeRef}
            aria-label={`${column.title} column`}
            className={`w-[280px] shrink-0 bg-[#F1F5F3] rounded-[12px] border flex flex-col max-h-full transition-colors duration-150 ${
                isOver
                    ? "border-[#7E9181] ring-2 ring-[#7E9181]/20 bg-[#EBF0EC]"
                    : "border-[#E2E8F0]"
            }`}
        >
            {/* Column Header */}
            <header className="p-3 border-b border-[#E2E8F0] bg-white/70 backdrop-blur-xs rounded-t-[12px] flex items-center justify-between">
                <div className="flex items-center gap-2">
                    <span className={`w-2 h-2 rounded-full ${column.dotColor}`} />
                    <h3 className="font-semibold text-[13px] text-[#1E293B] tracking-tight">
                        {column.title}
                    </h3>
                </div>

                <span
                    className={`text-[11px] font-semibold px-2 py-0.5 rounded-full ${column.badgeBg} ${column.badgeText}`}
                >
          {applications.length}
        </span>
            </header>

            {/* Cards List / Drop Target */}
            <div className="p-3 flex-1 overflow-y-auto space-y-3 min-h-[420px]">
                {applications.length > 0 ? (
                    applications.map((app) => (
                        <ApplicationCard key={app.id} application={app} onClick={onCardClick} />
                    ))
                ) : (
                    <div
                        className={`h-28 border border-dashed rounded-[10px] flex items-center justify-center text-center p-3 transition-colors ${
                            isOver ? "border-[#7E9181] bg-white/50" : "border-[#CBD5E1]"
                        }`}
                    >
            <span className="text-[12px] text-[#94A3B8]">
              {isOver ? "Drop here" : "No applications"}
            </span>
                    </div>
                )}
            </div>
        </section>
    );
}