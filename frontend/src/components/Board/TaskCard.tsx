import { useState } from 'react';
import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import type { Task } from '../../types';
import { cn } from '../../lib/utils';
import { GripVertical, Pencil, Trash2 } from 'lucide-react';
import { useTaskStore } from '../../store/taskStore';
import EditTaskModal from './EditTaskModal';

interface TaskCardProps {
  task: Task;
}

export default function TaskCard({ task }: TaskCardProps) {
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const deleteTask = useTaskStore((state) => state.deleteTask);

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

  const handleDelete = async (e: React.MouseEvent) => {
    e.stopPropagation();
    if (window.confirm('정말 삭제하시겠습니까?')) {
      try {
        await deleteTask(task.id);
      } catch {
        alert('삭제에 실패했습니다.');
      }
    }
  };

  const handleEdit = (e: React.MouseEvent) => {
    e.stopPropagation();
    setIsEditModalOpen(true);
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
        <h3 className='font-semibold text-slate-200 line-clamp-2 flex-1'>
          {task.title}
        </h3>
        <div className='flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity'>
          <button
            onClick={handleEdit}
            className='p-1 text-slate-500 hover:text-blue-400 transition-colors'
            title='수정'
          >
            <Pencil size={14} />
          </button>
          <button
            onClick={handleDelete}
            className='p-1 text-slate-500 hover:text-red-400 transition-colors'
            title='삭제'
          >
            <Trash2 size={14} />
          </button>
          <button className='p-1 text-slate-500 hover:text-slate-300 cursor-grab'>
            <GripVertical size={14} />
          </button>
        </div>
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

      <EditTaskModal
        task={task}
        isOpen={isEditModalOpen}
        onClose={() => setIsEditModalOpen(false)}
      />
    </div>
  );
}
