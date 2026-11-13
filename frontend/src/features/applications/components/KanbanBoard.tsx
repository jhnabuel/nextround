import { useState } from "react";
import {
    DndContext,
    DragOverlay,
    PointerSensor,
    useSensor,
    useSensors,
    type DragEndEvent,
    type DragStartEvent,
} from "@dnd-kit/core";
import { KANBAN_COLUMNS } from "../types/kanban.types";
import type { KanbanApplication } from "../types/kanban.types";
import type { ApplicationStatus } from "../types/application.types";
import { KanbanColumn } from "./KanbanColumn";
import { ApplicationCard } from "./ApplicationCard";

interface KanbanBoardProps {
    applications: KanbanApplication[];
    onCardClick?: (id: string) => void;
    onStatusChange?: (id: string, newStatus: ApplicationStatus) => void;
}

export function KanbanBoard({ applications, onCardClick, onStatusChange }: KanbanBoardProps) {
    const [activeId, setActiveId] = useState<string | null>(null);

    // Require 8px movement before starting drag so clicking still opens the edit modal
    const sensors = useSensors(
        useSensor(PointerSensor, {
            activationConstraint: {
                distance: 8,
            },
        })
    );

    function handleDragStart(event: DragStartEvent) {
        setActiveId(String(event.active.id));
    }

    function handleDragEnd(event: DragEndEvent) {
        const { active, over } = event;
        setActiveId(null);

        if (!over) return;

        const applicationId = String(active.id);
        const targetStatus = (over.data.current?.status ?? over.id) as ApplicationStatus;

        const isValidColumn = KANBAN_COLUMNS.some((col) => col.id === targetStatus);
        if (!isValidColumn) return;

        const draggedApp = applications.find((app) => app.id === applicationId);
        if (draggedApp && draggedApp.status !== targetStatus) {
            onStatusChange?.(applicationId, targetStatus);
        }
    }

    const activeApplication = activeId
        ? applications.find((app) => app.id === activeId) ?? null
        : null;

    return (
        <DndContext
            sensors={sensors}
            onDragStart={handleDragStart}
            onDragEnd={handleDragEnd}
            onDragCancel={() => setActiveId(null)}
        >
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

            <DragOverlay>
                {activeApplication ? (
                    <div className="rotate-2 opacity-95 shadow-xl cursor-grabbing">
                        <ApplicationCard application={activeApplication} />
                    </div>
                ) : null}
            </DragOverlay>
        </DndContext>
    );
}