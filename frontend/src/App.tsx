import { useMemo, useState } from 'react';
import {
  QueryClient,
  QueryClientProvider,
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query';
import {
  Todo,
  TodoPayload,
  TodoStatus,
  createTodo,
  deleteTodo,
  listTodos,
  updateStatus,
  updateTodo,
} from './api/todos';

const client = new QueryClient();

const statusOptions: TodoStatus[] = ['PENDING', 'IN_PROGRESS', 'DONE'];

function TodoForm({ onSubmit, initial }: { onSubmit: (payload: TodoPayload) => void; initial?: Todo }) {
  const [title, setTitle] = useState(initial?.title ?? '');
  const [description, setDescription] = useState(initial?.description ?? '');
  const [status, setStatus] = useState<TodoStatus>(initial?.status ?? 'PENDING');
  const [dueDate, setDueDate] = useState(initial?.dueDate?.slice(0, 10) ?? '');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSubmit({
      title,
      description: description || undefined,
      status,
      dueDate: dueDate || null,
    });
  };

  return (
    <form className="form" onSubmit={handleSubmit}>
      <div className="field">
        <label htmlFor="title">Title</label>
        <input
          id="title"
          required
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="What needs to get done?"
        />
      </div>
      <div className="field">
        <label htmlFor="description">Description</label>
        <textarea
          id="description"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          placeholder="Add details or context"
        />
      </div>
      <div className="field">
        <label htmlFor="status">Status</label>
        <select id="status" value={status} onChange={(e) => setStatus(e.target.value as TodoStatus)}>
          {statusOptions.map((s) => (
            <option key={s} value={s}>
              {s.replace('_', ' ')}
            </option>
          ))}
        </select>
      </div>
      <div className="field">
        <label htmlFor="dueDate">Due date</label>
        <input
          id="dueDate"
          type="date"
          value={dueDate}
          onChange={(e) => setDueDate(e.target.value)}
        />
      </div>
      <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
        <button className="btn" type="submit">
          {initial ? 'Update' : 'Add todo'}
        </button>
      </div>
    </form>
  );
}

function TodoList() {
  const queryClient = useQueryClient();
  const [filter, setFilter] = useState<TodoStatus | 'ALL'>('ALL');

  const { data, isLoading } = useQuery({
    queryKey: ['todos', filter],
    queryFn: () => listTodos(filter === 'ALL' ? undefined : { status: filter }),
  });

  const createMutation = useMutation({
    mutationFn: createTodo,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['todos'] }),
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: TodoPayload }) => updateTodo(id, payload),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['todos'] }),
  });

  const statusMutation = useMutation({
    mutationFn: ({ id, status }: { id: string; status: TodoStatus }) => updateStatus(id, status),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['todos'] }),
  });

  const deleteMutation = useMutation({
    mutationFn: deleteTodo,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['todos'] }),
  });

  const [editing, setEditing] = useState<Todo | null>(null);

  const sorted = useMemo(() => data ?? [], [data]);

  const onCreate = (payload: TodoPayload) => {
    createMutation.mutate(payload);
  };

  const onUpdate = (payload: TodoPayload) => {
    if (editing) {
      updateMutation.mutate({ id: editing.id, payload });
      setEditing(null);
    }
  };

  const renderActions = (todo: Todo) => (
    <div className="todo-actions">
      {statusOptions
        .filter((s) => s !== todo.status)
        .map((s) => (
          <button
            key={s}
            className="btn secondary"
            onClick={() => statusMutation.mutate({ id: todo.id, status: s })}
          >
            Mark {s.replace('_', ' ').toLowerCase()}
          </button>
        ))}
      <button className="btn secondary" onClick={() => setEditing(todo)}>
        Edit
      </button>
      <button className="btn secondary" onClick={() => deleteMutation.mutate(todo.id)}>
        Delete
      </button>
    </div>
  );

  return (
    <>
      <div className="card">
        <div className="header">
          <div>
            <div className="title">Create a task</div>
            <div className="muted">Capture what needs to happen</div>
          </div>
        </div>
        <TodoForm onSubmit={onCreate} />
      </div>

      {editing && (
        <div className="card">
          <div className="header">
            <div>
              <div className="title">Edit task</div>
              <div className="muted">Update details, status, or due date</div>
            </div>
            <button className="btn secondary" onClick={() => setEditing(null)}>
              Close
            </button>
          </div>
          <TodoForm onSubmit={onUpdate} initial={editing} />
        </div>
      )}

      <div className="card">
        <div className="header">
          <div>
            <div className="title">Your todos</div>
            <div className="muted">Filter by status to focus</div>
          </div>
          <div className="filters">
            <select value={filter} onChange={(e) => setFilter(e.target.value as TodoStatus | 'ALL')}>
              <option value="ALL">All</option>
              {statusOptions.map((s) => (
                <option key={s} value={s}>
                  {s.replace('_', ' ')}
                </option>
              ))}
            </select>
          </div>
        </div>

        {isLoading && <div className="muted">Loading...</div>}
        {!isLoading && sorted.length === 0 && <div className="muted">Nothing here yet.</div>}
        <div className="todo-list">
          {sorted.map((todo) => (
            <div key={todo.id} className="todo-item">
              <div className="todo-top">
                <div>
                  <div style={{ fontWeight: 700 }}>{todo.title}</div>
                  {todo.description && <div className="muted">{todo.description}</div>}
                </div>
                <div className={`status ${todo.status}`}>{todo.status.replace('_', ' ')}</div>
              </div>
              <div className="muted">
                Created {new Date(todo.createdAt).toLocaleString()}
                {todo.dueDate ? ` • Due ${new Date(todo.dueDate).toLocaleDateString()}` : ''}
              </div>
              {renderActions(todo)}
            </div>
          ))}
        </div>
      </div>
    </>
  );
}

export default function App() {
  return (
    <QueryClientProvider client={client}>
      <div className="app">
        <TodoList />
      </div>
    </QueryClientProvider>
  );
}
