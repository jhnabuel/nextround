import { KANBAN_COLUMNS } from "../types/kanban.types";
import type { KanbanApplication } from "../types/kanban.types";
import { KanbanColumn } from "./KanbanColumn";

interface KanbanBoardProps {
    applications: KanbanApplication[];
    onCardClick?: (id: string) => void;
}

export function KanbanBoard({ applications, onCardClick }: KanbanBoardProps) {
    return (
        <div className="flex-1 flex gap-4 overflow-x-auto pb-4 pt-1 items-start">
            {KANBAN_COLUMNS.map((col) => {
                const columnApps = applications.filter((app) => app.status === col.id);
                return (
                    <KanbanColumn
                        key={col.id}
                        column={col}
                        applications={columnApps}
                        onCardClick={onCardClick}
                    />
                );
            })}
        </div>
    );
}