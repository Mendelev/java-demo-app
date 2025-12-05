import { api } from './client';

export type TodoStatus = 'PENDING' | 'IN_PROGRESS' | 'DONE';

export interface Todo {
  id: string;
  title: string;
  description?: string;
  status: TodoStatus;
  dueDate?: string;
  createdAt: string;
  updatedAt: string;
}

export interface TodoPayload {
  title: string;
  description?: string;
  status?: TodoStatus;
  dueDate?: string | null;
}

export async function listTodos(params?: {
  status?: TodoStatus;
  fromDate?: string;
  toDate?: string;
}): Promise<Todo[]> {
  const { data } = await api.get('/todos', { params });
  return data;
}

export async function createTodo(payload: TodoPayload): Promise<Todo> {
  const { data } = await api.post('/todos', payload);
  return data;
}

export async function updateTodo(id: string, payload: TodoPayload): Promise<Todo> {
  const { data } = await api.put(`/todos/${id}`, payload);
  return data;
}

export async function updateStatus(id: string, status: TodoStatus): Promise<Todo> {
  const { data } = await api.post(`/todos/${id}/status`, { status });
  return data;
}

export async function deleteTodo(id: string): Promise<void> {
  await api.delete(`/todos/${id}`);
}
