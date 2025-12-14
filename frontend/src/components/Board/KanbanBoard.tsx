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
      const status = ['TODO', 'IN_PROGRESS', 'DONE'].includes(task.status)
        ? task.status
        : 'TODO';
      grouped[status].push(task);
    });
    // 각 컬럼의 태스크를 order로 정렬
    Object.keys(grouped).forEach((key) => {
      grouped[key as TaskStatus].sort((a, b) => a.order - b.order);
    });
    return grouped;
  }, [tasks]);

  const onDragStart = (event: DragStartEvent) => {
    if (event.active.data.current?.type === 'Task') {
      setActiveTask(event.active.data.current.task);
    }
  };

  const calculateNewOrder = (
    columnTasks: Task[],
    overTaskId: number | null,
    activeTaskId: number
  ): number => {
    const filteredTasks = columnTasks.filter((t) => t.id !== activeTaskId);

    if (filteredTasks.length === 0) {
      return Date.now();
    }

    if (overTaskId === null) {
      // 컬럼 끝에 드롭
      return filteredTasks[filteredTasks.length - 1].order + 1000;
    }

    const overIndex = filteredTasks.findIndex((t) => t.id === overTaskId);
    if (overIndex === -1) {
      return Date.now();
    }

    if (overIndex === 0) {
      // 첫 번째 위치에 드롭
      return filteredTasks[0].order - 1000;
    }

    // 중간에 드롭 - 이전 태스크와 현재 위치 태스크 사이의 중간값
    const prevOrder = filteredTasks[overIndex - 1].order;
    const currentOrder = filteredTasks[overIndex].order;
    return (prevOrder + currentOrder) / 2;
  };

  const onDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;
    if (!over) {
      setActiveTask(null);
      return;
    }

    const activeId = active.id as number;
    const overId = over.id;

    const isActiveTask = active.data.current?.type === 'Task';

    if (isActiveTask && activeTask) {
      let newStatus: TaskStatus;
      let overTaskId: number | null = null;

      // 컬럼에 직접 드롭된 경우
      if (COLUMNS.some((c) => c.id === overId)) {
        newStatus = overId as TaskStatus;
      }
      // 다른 태스크 위에 드롭된 경우
      else {
        const overTask = tasks.find((t) => t.id === overId);
        if (overTask) {
          newStatus = overTask.status;
          overTaskId = overTask.id;
        } else {
          setActiveTask(null);
          return;
        }
      }

      const columnTasks = tasksByColumn[newStatus];
      const newOrder = calculateNewOrder(columnTasks, overTaskId, activeId);

      // 상태나 순서가 변경된 경우에만 업데이트
      if (activeTask.status !== newStatus || activeTask.order !== newOrder) {
        moveTask(activeId, newStatus, newOrder);
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
