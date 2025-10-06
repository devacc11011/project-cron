'use client';

import { useState } from 'react';
import { api, ScheduleRequest } from '@/lib/api';
import { X, Calendar, Clock } from 'lucide-react';

interface ScheduleModalProps {
  taskId: number;
  taskTitle: string;
  onClose: () => void;
  onSuccess: () => void;
}

const CRON_PRESETS = [
  { label: 'Every Minute', value: '0 * * * * ?' },
  { label: 'Every Hour', value: '0 0 * * * ?' },
  { label: 'Daily at 9 AM', value: '0 0 9 * * ?' },
  { label: 'Daily at 6 PM', value: '0 0 18 * * ?' },
  { label: 'Every Monday 9 AM', value: '0 0 9 ? * MON' },
  { label: 'Custom', value: '' },
];

export default function ScheduleModal({ taskId, taskTitle, onClose, onSuccess }: ScheduleModalProps) {
  const [preset, setPreset] = useState('0 0 9 * * ?');
  const [cronExpression, setCronExpression] = useState('0 0 9 * * ?');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [loading, setLoading] = useState(false);

  const handlePresetChange = (value: string) => {
    setPreset(value);
    if (value) {
      setCronExpression(value);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!cronExpression.trim()) return;

    setLoading(true);
    try {
      const request: ScheduleRequest = {
        taskId,
        cronExpression,
        startDate: startDate || undefined,
        endDate: endDate || undefined,
      };

      await api.createSchedule(request);
      onSuccess();
      onClose();
    } catch (error) {
      console.error('Failed to create schedule:', error);
      alert('Failed to create schedule');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-zinc-900 border border-zinc-800 rounded-lg p-6 max-w-2xl w-full mx-4 shadow-2xl">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-2xl font-bold bg-gradient-to-r from-blue-400 to-purple-500 bg-clip-text text-transparent">
            Schedule Task
          </h2>
          <button
            onClick={onClose}
            className="p-2 hover:bg-zinc-800 rounded-lg transition"
          >
            <X className="w-5 h-5 text-gray-400" />
          </button>
        </div>

        <div className="mb-4 p-3 bg-black border border-zinc-800 rounded-lg">
          <p className="text-sm text-gray-400">Task:</p>
          <p className="text-white font-medium">{taskTitle}</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-300 mb-2">
              Schedule Preset
            </label>
            <select
              value={preset}
              onChange={(e) => handlePresetChange(e.target.value)}
              className="w-full px-4 py-2 bg-black border border-zinc-700 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              {CRON_PRESETS.map((p) => (
                <option key={p.label} value={p.value}>
                  {p.label}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-300 mb-2">
              Cron Expression
            </label>
            <input
              type="text"
              value={cronExpression}
              onChange={(e) => {
                setCronExpression(e.target.value);
                setPreset('');
              }}
              placeholder="0 0 9 * * ? (9 AM daily)"
              className="w-full px-4 py-2 bg-black border border-zinc-700 rounded-lg text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
            />
            <p className="mt-1 text-xs text-gray-500">
              Format: second minute hour day month weekday
            </p>
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
                className="w-full px-4 py-2 bg-black border border-zinc-700 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
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
                className="w-full px-4 py-2 bg-black border border-zinc-700 rounded-lg text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
          </div>

          <div className="flex gap-3 pt-4">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 px-4 py-2 bg-zinc-800 hover:bg-zinc-700 text-white font-semibold rounded-lg transition"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="flex-1 bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-500 hover:to-purple-500 text-white font-semibold py-2 px-4 rounded-lg transition disabled:opacity-50"
            >
              {loading ? 'Creating...' : 'Create Schedule'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
