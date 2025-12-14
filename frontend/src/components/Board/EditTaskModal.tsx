import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { X } from 'lucide-react';
import Input from '../ui/Input';
import Button from '../ui/Button';
import { useTaskStore } from '../../store/taskStore';
import type { Task, UpdateTaskRequest } from '../../types';

interface EditTaskModalProps {
  task: Task;
  isOpen: boolean;
  onClose: () => void;
}

export default function EditTaskModal({
  task,
  isOpen,
  onClose,
}: EditTaskModalProps) {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<UpdateTaskRequest>({
    defaultValues: {
      title: task.title,
      description: task.description || '',
    },
  });
  const updateTask = useTaskStore((state) => state.updateTask);

  useEffect(() => {
    if (isOpen) {
      reset({
        title: task.title,
        description: task.description || '',
      });
    }
  }, [isOpen, task, reset]);

  if (!isOpen) return null;

  const onSubmit = async (data: UpdateTaskRequest) => {
    try {
      await updateTask(task.id, data);
      onClose();
    } catch {
      alert('작업 수정에 실패했습니다');
    }
  };

  const handleBackdropClick = (e: React.MouseEvent) => {
    if (e.target === e.currentTarget) {
      onClose();
    }
  };

  return (
    <div
      className='fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm'
      onClick={handleBackdropClick}
    >
      <div className='bg-surface w-full max-w-md p-6 rounded-2xl border border-slate-700 shadow-2xl animate-in zoom-in-95 duration-200'>
        <div className='flex justify-between items-center mb-6'>
          <h2 className='text-xl font-bold text-white'>작업 수정</h2>
          <button
            onClick={onClose}
            className='text-slate-400 hover:text-white transition-colors'
          >
            <X size={24} />
          </button>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className='space-y-4'>
          <Input
            label='제목'
            {...register('title', { required: '제목을 입력해주세요' })}
            error={errors.title?.message as string}
            placeholder='할 일을 입력하세요'
            autoFocus
          />

          <div className='space-y-1.5'>
            <label className='text-sm font-medium text-slate-300 ml-1'>
              설명
            </label>
            <textarea
              {...register('description')}
              className='w-full px-4 py-3 bg-slate-900/50 border border-slate-700 rounded-lg focus:ring-2 focus:ring-primary/50 focus:border-primary outline-none transition-all placeholder-slate-500 text-white min-h-[100px] resize-none'
              placeholder='상세 내용을 입력하세요...'
            />
          </div>

          <div className='flex justify-end gap-3 mt-6'>
            <Button type='button' variant='ghost' onClick={onClose}>
              취소
            </Button>
            <Button type='submit'>저장</Button>
          </div>
        </form>
      </div>
    </div>
  );
}
