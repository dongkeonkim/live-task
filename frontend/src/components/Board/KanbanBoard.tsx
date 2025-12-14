import { useEffect, useState, useMemo } from 'react';
import {
  DndContext,
  DragOverlay,
  useSensors,
  useSensor,
  PointerSensor,
} from '@dnd-kit/core';
import type { DragStartEvent, DragEndEvent } from '@dnd-kit/core';
import { createPortal } from 'react-dom';
import type { Task, TaskStatus } from '../../types';
import Column from './Column';
import TaskCard from './TaskCard';
import { useTaskStore } from '../../store/taskStore';

const COLUMNS: { id: TaskStatus; title: string }[] = [
  { id: 'TODO', title: '할 일' },
  { id: 'IN_PROGRESS', title: '진행 중' },
  { id: 'DONE', title: '완료' },
];

export default function KanbanBoard() {
  const { tasks, fetchTasks, moveTask } = useTaskStore();
  const [activeTask, setActiveTask] = useState<Task | null>(null);

  useEffect(() => {
    fetchTasks();
  }, [fetchTasks]);

  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: {
        distance: 3, // 드래그 시작에 3px 이동 필요
      },
    })
  );

  const tasksByColumn = useMemo(() => {
    const grouped: Record<TaskStatus, Task[]> = {
      TODO: [],
      IN_PROGRESS: [],
      DONE: [],
    };
    tasks.forEach((task) => {
      // 유효하지 않은 status는 TODO로 대체
      const status = ['TODO', 'IN_PROGRESS', 'DONE'].includes(task.status)
        ? task.status
        : 'TODO';
      grouped[status].push(task);
    });
    return grouped;
  }, [tasks]);

  const onDragStart = (event: DragStartEvent) => {
    if (event.active.data.current?.type === 'Task') {
      setActiveTask(event.active.data.current.task);
    }
  };

  const onDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;
    if (!over) return;

    const activeId = active.id as number;
    const overId = over.id; // 컬럼 ID(string) 또는 태스크 ID(number)

    const isActiveTask = active.data.current?.type === 'Task';

    if (isActiveTask) {
      let newStatus: TaskStatus | undefined;

      // 컬럼에 직접 드롭된 경우
      if (COLUMNS.some((c) => c.id === overId)) {
        newStatus = overId as TaskStatus;
      }
      // 다른 태스크 위에 드롭된 경우
      else {
        const overTask = tasks.find((t) => t.id === overId);
        if (overTask) {
          newStatus = overTask.status;
        }
      }

      if (newStatus && activeTask && activeTask.status !== newStatus) {
        moveTask(activeId, Number(overId), newStatus);
      }
    }

    setActiveTask(null);
  };

  return (
    <DndContext
      sensors={sensors}
      onDragStart={onDragStart}
      onDragEnd={onDragEnd}
    >
      <div className='flex gap-6 h-full overflow-x-auto pb-4'>
        {COLUMNS.map((col) => (
          <Column
            key={col.id}
            id={col.id}
            title={col.title}
            tasks={tasksByColumn[col.id]}
          />
        ))}
      </div>

      {createPortal(
        <DragOverlay>
          {activeTask && <TaskCard task={activeTask} />}
        </DragOverlay>,
        document.body
      )}
    </DndContext>
  );
}
