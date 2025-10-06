'use client';

import { useEffect, useState } from 'react';
import { api, Task } from '@/lib/api';
import { Play, Trash2, Clock, CheckCircle, XCircle, Loader2, CalendarClock } from 'lucide-react';
import ScheduleModal from './schedule-modal';

export default function TaskList() {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState(true);
  const [title, setTitle] = useState('');
  const [prompt, setPrompt] = useState('');
  const [aiProvider, setAiProvider] = useState('gemini');
  const [enableWebSearch, setEnableWebSearch] = useState(false);
  const [notificationType, setNotificationType] = useState('discord');
  const [creating, setCreating] = useState(false);
  const [scheduleModal, setScheduleModal] = useState<{ taskId: number; taskTitle: string } | null>(null);

  const loadTasks = async () => {
    try {
      const data = await api.getAllTasks();
      setTasks(data);
    } catch (error) {
      console.error('Failed to load tasks:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadTasks();
  }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim() || !prompt.trim()) return;

    setCreating(true);
    try {
      await api.createTask({ title, prompt, aiProvider, enableWebSearch, notificationType });
      setTitle('');
      setPrompt('');
      setAiProvider('gemini');
      setEnableWebSearch(false);
      setNotificationType('discord');
      await loadTasks();
    } catch (error) {
      console.error('Failed to create task:', error);
    } finally {
      setCreating(false);
    }
  };

  const handleExecute = async (id: number) => {
    try {
      await api.executeTask(id);
      await loadTasks();
      // Poll for updates
      const interval = setInterval(async () => {
        await loadTasks();
      }, 2000);
      setTimeout(() => clearInterval(interval), 60000); // Stop after 1 minute
    } catch (error) {
      console.error('Failed to execute task:', error);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Are you sure you want to delete this task?')) return;

    try {
      await api.deleteTask(id);
      await loadTasks();
    } catch (error) {
      console.error('Failed to delete task:', error);
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'PENDING':
        return <Clock className="w-5 h-5 text-yellow-400" />;
      case 'PROCESSING':
        return <Loader2 className="w-5 h-5 text-blue-400 animate-spin" />;
      case 'COMPLETED':
        return <CheckCircle className="w-5 h-5 text-green-400" />;
      case 'FAILED':
        return <XCircle className="w-5 h-5 text-red-400" />;
      default:
        return null;
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PENDING':
        return 'text-yellow-400';
      case 'PROCESSING':
        return 'text-blue-400';
      case 'COMPLETED':
        return 'text-green-400';
      case 'FAILED':
        return 'text-red-400';
      default:
        return 'text-gray-400';
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <Loader2 className="w-8 h-8 text-blue-400 animate-spin" />
      </div>
    );
  }

  return (
    <div className="space-y-8">
      {/* Create Task Form */}
      <div className="bg-zinc-900 border border-zinc-800 rounded-lg p-6 shadow-lg">
        <h2 className="text-xl font-bold mb-4 bg-gradient-to-r from-blue-400 to-purple-500 bg-clip-text text-transparent">
          Create New Task
        </h2>
        <form onSubmit={handleCreate} className="space-y-4">
          <div>
            <label htmlFor="title" className="block text-sm font-medium text-gray-300 mb-2">
              Title
            </label>
            <input
              id="title"
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="e.g., Summarize latest tech news"
              className="w-full px-4 py-2 bg-black border border-zinc-700 rounded-lg text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition"
              required
            />
          </div>
          <div>
            <label htmlFor="aiProvider" className="block text-sm font-medium text-gray-300 mb-2">
              AI Provider
            </label>
            <select
              id="aiProvider"
              value={aiProvider}
              onChange={(e) => setAiProvider(e.target.value)}
              className="w-full px-4 py-2 bg-black border border-zinc-700 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition"
            >
              <option value="gemini">Gemini</option>
              <option value="claude">Claude</option>
              <option value="chatgpt">ChatGPT</option>
            </select>
          </div>
          <div>
            <label htmlFor="prompt" className="block text-sm font-medium text-gray-300 mb-2">
              Prompt
            </label>
            <textarea
              id="prompt"
              value={prompt}
              onChange={(e) => setPrompt(e.target.value)}
              placeholder="Enter the task prompt for AI..."
              rows={4}
              className="w-full px-4 py-2 bg-black border border-zinc-700 rounded-lg text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition resize-none"
              required
            />
          </div>
          <div className="flex items-center gap-3">
            <input
              id="enableWebSearch"
              type="checkbox"
              checked={enableWebSearch}
              onChange={(e) => setEnableWebSearch(e.target.checked)}
              className="w-4 h-4 bg-black border border-zinc-700 rounded focus:ring-2 focus:ring-blue-500 text-blue-600"
            />
            <label htmlFor="enableWebSearch" className="text-sm font-medium text-gray-300 cursor-pointer">
              Enable Web Search (for latest information)
            </label>
          </div>
          <div>
            <label htmlFor="notificationType" className="block text-sm font-medium text-gray-300 mb-2">
              Notification Type
            </label>
            <select
              id="notificationType"
              value={notificationType}
              onChange={(e) => setNotificationType(e.target.value)}
              className="w-full px-4 py-2 bg-black border border-zinc-700 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition"
            >
              <option value="discord">Discord</option>
              <option value="email">Email (Coming Soon)</option>
            </select>
          </div>
          <button
            type="submit"
            disabled={creating}
            className="w-full bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-500 hover:to-purple-500 text-white font-semibold py-2 px-4 rounded-lg transition disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {creating ? 'Creating...' : 'Create Task'}
          </button>
        </form>
      </div>

      {/* Tasks List */}
      <div className="space-y-4">
        <h2 className="text-2xl font-bold bg-gradient-to-r from-blue-400 to-purple-500 bg-clip-text text-transparent">
          Tasks
        </h2>
        {tasks.length === 0 ? (
          <div className="bg-zinc-900 border border-zinc-800 rounded-lg p-8 text-center">
            <p className="text-gray-400">No tasks yet. Create your first task above!</p>
          </div>
        ) : (
          tasks.map((task) => (
            <div
              key={task.id}
              className="bg-zinc-900 border border-zinc-800 rounded-lg p-6 shadow-lg hover:border-zinc-700 transition"
            >
              <div className="flex items-start justify-between mb-4">
                <div className="flex-1">
                  <div className="flex items-center gap-3 mb-2">
                    {getStatusIcon(task.status)}
                    <h3 className="text-lg font-semibold text-white">{task.title}</h3>
                    <span className={`text-sm font-medium ${getStatusColor(task.status)}`}>
                      {task.status}
                    </span>
                    <span className="text-xs px-2 py-1 bg-gradient-to-r from-blue-500 to-purple-500 rounded-full text-white font-medium">
                      {task.aiProvider.toUpperCase()}
                    </span>
                    {task.enableWebSearch && (
                      <span className="text-xs px-2 py-1 bg-gradient-to-r from-green-500 to-emerald-500 rounded-full text-white font-medium">
                        WEB SEARCH
                      </span>
                    )}
                  </div>
                  <p className="text-sm text-gray-400">
                    Created by {task.user.username} • {new Date(task.createdAt).toLocaleString()}
                  </p>
                </div>
                <div className="flex gap-2">
                  <button
                    onClick={() => setScheduleModal({ taskId: task.id, taskTitle: task.title })}
                    className="p-2 bg-purple-600 hover:bg-purple-500 text-white rounded-lg transition"
                    title="Schedule Task"
                  >
                    <CalendarClock className="w-4 h-4" />
                  </button>
                  {task.status === 'PENDING' && (
                    <button
                      onClick={() => handleExecute(task.id)}
                      className="p-2 bg-blue-600 hover:bg-blue-500 text-white rounded-lg transition"
                      title="Execute Task"
                    >
                      <Play className="w-4 h-4" />
                    </button>
                  )}
                  <button
                    onClick={() => handleDelete(task.id)}
                    className="p-2 bg-red-600 hover:bg-red-500 text-white rounded-lg transition"
                    title="Delete Task"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>

              <div className="space-y-3">
                <div>
                  <p className="text-xs font-medium text-gray-500 mb-1">PROMPT</p>
                  <p className="text-sm text-gray-300 bg-black rounded p-3 border border-zinc-800">
                    {task.prompt}
                  </p>
                </div>

                {task.result && (
                  <div>
                    <p className="text-xs font-medium text-gray-500 mb-1">RESULT</p>
                    <div className="text-sm text-gray-300 bg-black rounded p-3 border border-zinc-800 whitespace-pre-wrap">
                      {task.result}
                    </div>
                  </div>
                )}

                {task.executedAt && (
                  <p className="text-xs text-gray-500">
                    Executed at {new Date(task.executedAt).toLocaleString()}
                  </p>
                )}
              </div>
            </div>
          ))
        )}
      </div>

      {scheduleModal && (
        <ScheduleModal
          taskId={scheduleModal.taskId}
          taskTitle={scheduleModal.taskTitle}
          onClose={() => setScheduleModal(null)}
          onSuccess={() => loadTasks()}
        />
      )}
    </div>
  );
}
