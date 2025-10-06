'use client';

import { useEffect, useState } from 'react';
import { api, Task } from '@/lib/api';
import { Loader2, CheckCircle2, XCircle, Clock } from 'lucide-react';

export default function ScheduleHistoryPage() {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState(true);

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

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'COMPLETED':
        return <CheckCircle2 className="w-5 h-5 text-green-400" />;
      case 'FAILED':
        return <XCircle className="w-5 h-5 text-red-400" />;
      case 'PROCESSING':
        return <Loader2 className="w-5 h-5 text-blue-400 animate-spin" />;
      default:
        return <Clock className="w-5 h-5 text-gray-400" />;
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'COMPLETED':
        return 'bg-green-500/20 text-green-400';
      case 'FAILED':
        return 'bg-red-500/20 text-red-400';
      case 'PROCESSING':
        return 'bg-blue-500/20 text-blue-400';
      default:
        return 'bg-gray-500/20 text-gray-400';
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-black flex items-center justify-center">
        <Loader2 className="w-8 h-8 text-blue-400 animate-spin" />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-black">
      <div className="max-w-6xl mx-auto px-4 py-12">
        <div className="mb-8">
          <h1 className="text-4xl font-bold mb-3 bg-gradient-to-r from-purple-400 via-pink-500 to-red-500 bg-clip-text text-transparent">
            Execution History
          </h1>
          <p className="text-gray-400">View all scheduled task executions</p>
        </div>

        <div className="space-y-4">
          {tasks.length === 0 ? (
            <div className="bg-zinc-900 border border-zinc-800 rounded-lg p-8 text-center">
              <p className="text-gray-400">No execution history yet.</p>
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
                      <span className={`text-xs px-2 py-1 rounded-full font-medium ${getStatusColor(task.status)}`}>
                        {task.status}
                      </span>
                      <span className="text-xs px-2 py-1 bg-gradient-to-r from-purple-500 to-pink-500 rounded-full text-white font-medium">
                        {task.aiProvider.toUpperCase()}
                      </span>
                      {task.enableWebSearch && (
                        <span className="text-xs px-2 py-1 bg-gradient-to-r from-green-500 to-emerald-500 rounded-full text-white font-medium">
                          WEB SEARCH
                        </span>
                      )}
                    </div>
                    <div className="flex items-center gap-4 text-xs text-gray-500 mb-2">
                      <span>Created: {new Date(task.createdAt).toLocaleString()}</span>
                      {task.executedAt && (
                        <span>Executed: {new Date(task.executedAt).toLocaleString()}</span>
                      )}
                    </div>
                    <p className="text-xs text-gray-400">
                      By: <span className="text-purple-400">{task.user.username}</span>
                    </p>
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
                      <div className="text-sm text-gray-300 bg-black rounded p-3 border border-zinc-800 max-h-96 overflow-y-auto">
                        <pre className="whitespace-pre-wrap font-mono text-xs">{task.result}</pre>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}
