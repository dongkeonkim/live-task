import {
  SortableContext,
  verticalListSortingStrategy,
} from '@dnd-kit/sortable';
import { useDroppable } from '@dnd-kit/core';
import type { Task, TaskStatus } from '../../types';
import TaskCard from './TaskCard';
import { cn } from '../../lib/utils';

interface ColumnProps {
  id: TaskStatus;
  title: string;
  tasks: Task[];
}

export default function Column({ id, title, tasks }: ColumnProps) {
  const { setNodeRef } = useDroppable({
    id: id,
  });

  return (
    <div className='flex flex-col gap-4 w-full h-full min-w-[300px]'>
      <div className='flex items-center justify-between'>
        <h2 className='text-lg font-bold text-slate-300 flex items-center gap-2'>
          {title}
          <span className='bg-slate-800 text-slate-500 text-xs px-2 py-0.5 rounded-full'>
            {tasks.length}
          </span>
        </h2>
      </div>

      <div
        ref={setNodeRef}
        className={cn(
          'flex-1 flex flex-col gap-3 p-4 rounded-xl bg-slate-900/30 border border-slate-800/50 min-h-[500px]'
        )}
      >
        <SortableContext
          items={tasks.map((t) => t.id)}
          strategy={verticalListSortingStrategy}
        >
          {tasks.map((task) => (
            <TaskCard key={task.id} task={task} />
          ))}
          {tasks.length === 0 && (
            <div className='h-full flex items-center justify-center text-slate-600 text-sm border-2 border-dashed border-slate-800 rounded-lg'>
              여기에 항목을 놓으세요
            </div>
          )}
        </SortableContext>
      </div>
    </div>
  );
}
