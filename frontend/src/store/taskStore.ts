import { create } from 'zustand';
import api from '../lib/api';
import type {
  Task,
  CreateTaskRequest,
  UpdateTaskRequest,
  TaskStatus,
} from '../types';

interface TaskState {
  tasks: Task[];
  isLoading: boolean;
  error: string | null;
  fetchTasks: () => Promise<void>;
  createTask: (data: CreateTaskRequest) => Promise<void>;
  updateTask: (id: number, data: UpdateTaskRequest) => Promise<void>;
  deleteTask: (id: number) => Promise<void>;
  moveTask: (
    activeId: number,
    overId: number,
    newStatus: TaskStatus
  ) => Promise<void>; // Optimistic update support
}

export const useTaskStore = create<TaskState>((set, get) => ({
  tasks: [],
  isLoading: false,
  error: null,

  fetchTasks: async () => {
    set({ isLoading: true, error: null });
    try {
      const res = await api.get<Task[]>('/api/tasks');
      // Sort on client side just in case, though backend does it too.
      // But for Kanban, we might need to separate by columns.
      set({ tasks: res.data, isLoading: false });
    } catch {
      set({ error: '태스크를 불러오는데 실패했습니다.', isLoading: false });
    }
  },

  createTask: async (data) => {
    const res = await api.post<Task>('/api/tasks', data);
    set((state) => ({ tasks: [...state.tasks, res.data] }));
  },

  updateTask: async (id, data) => {
    // Optimistic update
    const previousTasks = get().tasks;
    set((state) => ({
      tasks: state.tasks.map((t) => (t.id === id ? { ...t, ...data } : t)),
    }));

    try {
      await api.put<Task>(`/api/tasks/${id}`, data);
      // Optionally refetch or update with real server data if needed
    } catch (err) {
      // Rollback
      set({ tasks: previousTasks });
      throw err;
    }
  },

  deleteTask: async (id) => {
    const previousTasks = get().tasks;
    set((state) => ({ tasks: state.tasks.filter((t) => t.id !== id) }));

    try {
      await api.delete(`/api/tasks/${id}`);
    } catch (err) {
      set({ tasks: previousTasks });
      throw err;
    }
  },

  moveTask: async (activeId, _overId, newStatus) => {
    // This is a simplified move. Complex reordering usually involves calculating new indices.
    // For this MVP, we just update the status. If we implemented full reordering, we'd need to update 'order' field too.
    // Let's assume for now we just change status.
    const task = get().tasks.find((t) => t.id === activeId);
    if (task && task.status !== newStatus) {
      await get().updateTask(activeId, { status: newStatus });
    }
  },
}));
