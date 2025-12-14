import { useState } from 'react';
import { useAuthStore } from '../store/authStore';
import Button from '../components/ui/Button';
import KanbanBoard from '../components/Board/KanbanBoard';
import NewTaskModal from '../components/Board/NewTaskModal';
import { Plus } from 'lucide-react';

export default function HomePage() {
  const { user, logout } = useAuthStore();
  const [isModalOpen, setIsModalOpen] = useState(false);

  return (
    <div className='min-h-screen bg-background p-8 flex flex-col'>
      <header className='flex justify-between items-center mb-8 shrink-0'>
        <div>
          <h1 className='text-3xl font-bold bg-gradient-to-r from-blue-400 to-violet-400 bg-clip-text text-transparent'>
            칸반 보드
          </h1>
          <p className='text-slate-400 mt-1'>
            안녕하세요, {user?.username}님 👋
          </p>
        </div>
        <div className='flex gap-4'>
          <Button onClick={() => setIsModalOpen(true)}>
            <Plus size={20} />새 작업
          </Button>
          <Button onClick={logout} variant='secondary'>
            로그아웃
          </Button>
        </div>
      </header>

      <main className='flex-1 overflow-hidden'>
        <KanbanBoard />
      </main>

      <NewTaskModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
      />
    </div>
  );
}
