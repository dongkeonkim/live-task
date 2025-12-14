import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import type { Task } from '../../types';
import { cn } from '../../lib/utils';
import { GripVertical } from 'lucide-react';

interface TaskCardProps {
  task: Task;
}

export default function TaskCard({ task }: TaskCardProps) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({
    id: task.id,
    data: {
      type: 'Task',
      task,
    },
  });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
  };

  if (isDragging) {
    return (
      <div
        ref={setNodeRef}
        style={style}
        className='opacity-30 bg-slate-800 border-2 border-primary border-dashed rounded-xl h-[100px] w-full'
      />
    );
  }

  return (
    <div
      ref={setNodeRef}
      style={style}
      {...attributes}
      {...listeners}
      className={cn(
        'group relative bg-surface p-4 rounded-xl border border-slate-700 shadow-sm hover:shadow-md hover:border-slate-600 transition-all cursor-grab active:cursor-grabbing',
        'flex flex-col gap-2'
      )}
    >
      <div className='flex justify-between items-start'>
        <h3 className='font-semibold text-slate-200 line-clamp-2'>
          {task.title}
        </h3>
        <button className='text-slate-600 hover:text-slate-400 opacity-0 group-hover:opacity-100 transition-opacity cursor-grab'>
          <GripVertical size={16} />
        </button>
      </div>

      {task.description && (
        <p className='text-sm text-slate-400 line-clamp-2'>
          {task.description}
        </p>
      )}

      <div className='mt-2 flex justify-between items-center text-xs text-slate-500'>
        <span className='bg-slate-800 px-2 py-1 rounded text-slate-400 font-medium'>
          {task.creatorName}
        </span>
        <span>{new Date(task.createdAt).toLocaleDateString()}</span>
      </div>
    </div>
  );
}
