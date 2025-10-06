'use client';

import { useEffect, useState } from 'react';
import { api, Schedule } from '@/lib/api';
import { CalendarClock, Trash2, Power, PowerOff, Loader2, Clock, Calendar } from 'lucide-react';

const CRON_PRESETS = [
  { label: 'Every Minute', value: '0 * * * * ?' },
  { label: 'Every Hour', value: '0 0 * * * ?' },
  { label: 'Daily at 9 AM', value: '0 0 9 * * ?' },
  { label: 'Daily at 6 PM', value: '0 0 18 * * ?' },
  { label: 'Every Monday 9 AM', value: '0 0 9 ? * MON' },
];

export default function SchedulesPage() {
  const [schedules, setSchedules] = useState<Schedule[]>([]);
  const [loading, setLoading] = useState(true);
  const [creating, setCreating] = useState(false);

  // Form state
  const [title, setTitle] = useState('');
  const [prompt, setPrompt] = useState('');
  const [aiProvider, setAiProvider] = useState('gemini');
  const [enableWebSearch, setEnableWebSearch] = useState(false);
  const [cronPreset, setCronPreset] = useState('0 0 9 * * ?');
  const [cronExpression, setCronExpression] = useState('0 0 9 * * ?');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');

  const loadSchedules = async () => {
    try {
      const data = await api.getMySchedules();
      setSchedules(data);
    } catch (error) {
      console.error('Failed to load schedules:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadSchedules();
  }, []);

  const handlePresetChange = (value: string) => {
    setCronPreset(value);
    setCronExpression(value);
  };

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim() || !prompt.trim() || !cronExpression.trim()) return;

    setCreating(true);
    try {
      await api.createMySchedule({
        title,
        prompt,
        aiProvider,
        enableWebSearch,
        cronExpression,
        startDate: startDate || undefined,
        endDate: endDate || undefined,
      });

      setTitle('');
      setPrompt('');
      setAiProvider('gemini');
      setEnableWebSearch(false);
      setCronExpression('0 0 9 * * ?');
      setCronPreset('0 0 9 * * ?');
      setStartDate('');
      setEndDate('');

      await loadSchedules();
    } catch (error) {
      console.error('Failed to create schedule:', error);
      alert('Failed to create schedule');
    } finally {
      setCreating(false);
    }
  };

  const handleToggle = async (id: number) => {
    try {
      await api.toggleSchedule(id);
      await loadSchedules();
    } catch (error) {
      console.error('Failed to toggle schedule:', error);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Are you sure you want to delete this schedule?')) return;

    try {
      await api.deleteSchedule(id);
      await loadSchedules();
    } catch (error) {
      console.error('Failed to delete schedule:', error);
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
            My Schedules
          </h1>
          <p className="text-gray-400">Manage your recurring AI tasks</p>
        </div>

        {/* Create Form */}
        <div className="bg-zinc-900 border border-zinc-800 rounded-lg p-6 shadow-lg mb-8">
          <h2 className="text-xl font-bold mb-4 bg-gradient-to-r from-purple-400 to-pink-500 bg-clip-text text-transparent">
            Create New Schedule
          </h2>
          <form onSubmit={handleCreate} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-2">Title</label>
              <input
                type="text"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="e.g., Daily News Summary"
                className="w-full px-4 py-2 bg-black border border-zinc-700 rounded-lg text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-purple-500"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-300 mb-2">Prompt</label>
              <textarea
                value={prompt}
                onChange={(e) => setPrompt(e.target.value)}
                placeholder="Enter your AI prompt..."
                rows={4}
                className="w-full px-4 py-2 bg-black border border-zinc-700 rounded-lg text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-purple-500 resize-none"
                required
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-2">AI Provider</label>
                <select
                  value={aiProvider}
                  onChange={(e) => setAiProvider(e.target.value)}
                  className="w-full px-4 py-2 bg-black border border-zinc-700 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-purple-500"
                >
                  <option value="gemini">Gemini</option>
                  <option value="claude">Claude</option>
                  <option value="chatgpt">ChatGPT</option>
                </select>
              </div>

              <div className="flex items-end">
                <label className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={enableWebSearch}
                    onChange={(e) => setEnableWebSearch(e.target.checked)}
                    className="w-4 h-4 bg-black border border-zinc-700 rounded focus:ring-2 focus:ring-purple-500"
                  />
                  <span className="text-sm font-medium text-gray-300">Enable Web Search</span>
                </label>
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-300 mb-2">Schedule Preset</label>
              <select
                value={cronPreset}
                onChange={(e) => handlePresetChange(e.target.value)}
                className="w-full px-4 py-2 bg-black border border-zinc-700 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-purple-500"
              >
                {CRON_PRESETS.map((preset) => (
                  <option key={preset.label} value={preset.value}>
                    {preset.label}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-300 mb-2">Cron Expression</label>
              <input
                type="text"
                value={cronExpression}
                onChange={(e) => {
                  setCronExpression(e.target.value);
                  setCronPreset('');
                }}
                placeholder="0 0 9 * * ?"
                className="w-full px-4 py-2 bg-black border border-zinc-700 rounded-lg text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-purple-500"
                required
              />
              <p className="mt-1 text-xs text-gray-500">Format: second minute hour day month weekday</p>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-2 flex items-center gap-2">
                  <Calendar className="w-4 h-4" />
                  Start Date (Optional)
                </label>
                <input
                  type="datetime-local"
                  value={startDate}
                  onChange={(e) => setStartDate(e.target.value)}
                  className="w-full px-4 py-2 bg-black border border-zinc-700 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-purple-500"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-300 mb-2 flex items-center gap-2">
                  <Clock className="w-4 h-4" />
                  End Date (Optional)
                </label>
                <input
                  type="datetime-local"
                  value={endDate}
                  onChange={(e) => setEndDate(e.target.value)}
                  className="w-full px-4 py-2 bg-black border border-zinc-700 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-purple-500"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={creating}
              className="w-full bg-gradient-to-r from-purple-600 to-pink-600 hover:from-purple-500 hover:to-pink-500 text-white font-semibold py-2 px-4 rounded-lg transition disabled:opacity-50"
            >
              {creating ? 'Creating...' : 'Create Schedule'}
            </button>
          </form>
        </div>

        {/* Schedules List */}
        <div className="space-y-4">
          <h2 className="text-2xl font-bold bg-gradient-to-r from-purple-400 to-pink-500 bg-clip-text text-transparent">
            Active Schedules
          </h2>

          {schedules.length === 0 ? (
            <div className="bg-zinc-900 border border-zinc-800 rounded-lg p-8 text-center">
              <p className="text-gray-400">No schedules yet. Create your first one above!</p>
            </div>
          ) : (
            schedules.map((schedule) => (
              <div
                key={schedule.id}
                className="bg-zinc-900 border border-zinc-800 rounded-lg p-6 shadow-lg hover:border-zinc-700 transition"
              >
                <div className="flex items-start justify-between mb-4">
                  <div className="flex-1">
                    <div className="flex items-center gap-3 mb-2">
                      <CalendarClock className="w-5 h-5 text-purple-400" />
                      <h3 className="text-lg font-semibold text-white">{schedule.title}</h3>
                      <span className={`text-xs px-2 py-1 rounded-full font-medium ${schedule.enabled ? 'bg-green-500/20 text-green-400' : 'bg-gray-500/20 text-gray-400'}`}>
                        {schedule.enabled ? 'Enabled' : 'Disabled'}
                      </span>
                      <span className="text-xs px-2 py-1 bg-gradient-to-r from-purple-500 to-pink-500 rounded-full text-white font-medium">
                        {schedule.aiProvider.toUpperCase()}
                      </span>
                      {schedule.enableWebSearch && (
                        <span className="text-xs px-2 py-1 bg-gradient-to-r from-green-500 to-emerald-500 rounded-full text-white font-medium">
                          WEB SEARCH
                        </span>
                      )}
                    </div>
                    <p className="text-sm text-gray-400 mb-2">
                      Cron: <code className="text-purple-400">{schedule.cronExpression}</code>
                    </p>
                    {schedule.lastExecutedAt && (
                      <p className="text-xs text-gray-500">
                        Last executed: {new Date(schedule.lastExecutedAt).toLocaleString()}
                      </p>
                    )}
                  </div>

                  <div className="flex gap-2">
                    <button
                      onClick={() => handleToggle(schedule.id)}
                      className={`p-2 ${schedule.enabled ? 'bg-yellow-600 hover:bg-yellow-500' : 'bg-green-600 hover:bg-green-500'} text-white rounded-lg transition`}
                      title={schedule.enabled ? 'Disable' : 'Enable'}
                    >
                      {schedule.enabled ? <PowerOff className="w-4 h-4" /> : <Power className="w-4 h-4" />}
                    </button>
                    <button
                      onClick={() => handleDelete(schedule.id)}
                      className="p-2 bg-red-600 hover:bg-red-500 text-white rounded-lg transition"
                      title="Delete"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>

                <div className="space-y-2">
                  <div>
                    <p className="text-xs font-medium text-gray-500 mb-1">PROMPT</p>
                    <p className="text-sm text-gray-300 bg-black rounded p-3 border border-zinc-800">
                      {schedule.prompt}
                    </p>
                  </div>

                  {(schedule.startDate || schedule.endDate) && (
                    <div className="flex gap-4 text-xs text-gray-500">
                      {schedule.startDate && (
                        <span>Start: {new Date(schedule.startDate).toLocaleString()}</span>
                      )}
                      {schedule.endDate && (
                        <span>End: {new Date(schedule.endDate).toLocaleString()}</span>
                      )}
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
