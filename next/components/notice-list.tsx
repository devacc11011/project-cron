'use client';

import { useState, useEffect } from 'react';
import { api, Notice, User } from '@/lib/api';

export default function NoticeList() {
  const [notices, setNotices] = useState<Notice[]>([]);
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [isCreating, setIsCreating] = useState(false);
  const [editingNotice, setEditingNotice] = useState<Notice | null>(null);
  const [formData, setFormData] = useState({ title: '', content: '' });

  const isAdmin = user?.roles.includes('ADMIN') ?? false;

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const [noticesData, userData] = await Promise.all([
        api.getAllNotices(),
        api.getCurrentUser(),
      ]);
      setNotices(noticesData);
      setUser(userData);
    } catch (error) {
      console.error('Failed to load data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.createNotice(formData);
      setFormData({ title: '', content: '' });
      setIsCreating(false);
      await loadData();
    } catch (error) {
      console.error('Failed to create notice:', error);
      alert('Failed to create notice');
    }
  };

  const handleUpdate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingNotice) return;

    try {
      await api.updateNotice(editingNotice.id, formData);
      setFormData({ title: '', content: '' });
      setEditingNotice(null);
      await loadData();
    } catch (error) {
      console.error('Failed to update notice:', error);
      alert('Failed to update notice');
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Are you sure you want to delete this notice?')) return;

    try {
      await api.deleteNotice(id);
      await loadData();
    } catch (error) {
      console.error('Failed to delete notice:', error);
      alert('Failed to delete notice');
    }
  };

  const startEdit = (notice: Notice) => {
    setEditingNotice(notice);
    setFormData({ title: notice.title, content: notice.content });
    setIsCreating(false);
  };

  const cancelEdit = () => {
    setEditingNotice(null);
    setIsCreating(false);
    setFormData({ title: '', content: '' });
  };

  if (loading) {
    return <div className="text-center py-8">Loading...</div>;
  }

  return (
    <div className="max-w-4xl mx-auto p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold text-slate-900 dark:text-white">
          Notices
        </h1>
        {isAdmin && !isCreating && !editingNotice && (
          <button
            onClick={() => setIsCreating(true)}
            className="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded shadow-lg"
          >
            Create Notice
          </button>
        )}
      </div>

      {(isCreating || editingNotice) && (
        <form
          onSubmit={editingNotice ? handleUpdate : handleCreate}
          className="bg-white dark:bg-slate-900 shadow-md rounded-lg px-8 pt-6 pb-8 mb-6 border border-slate-200 dark:border-slate-800"
        >
          <h2 className="text-xl font-bold mb-4 text-slate-900 dark:text-white">
            {editingNotice ? 'Edit Notice' : 'Create Notice'}
          </h2>
          <div className="mb-4">
            <label className="block text-slate-700 dark:text-slate-300 text-sm font-bold mb-2">
              Title
            </label>
            <input
              type="text"
              value={formData.title}
              onChange={(e) =>
                setFormData({ ...formData, title: e.target.value })
              }
              className="shadow appearance-none border border-slate-300 dark:border-slate-700 rounded w-full py-2 px-3 text-slate-900 dark:text-white bg-white dark:bg-slate-800 leading-tight focus:outline-none focus:shadow-outline focus:border-blue-500"
              required
            />
          </div>
          <div className="mb-4">
            <label className="block text-slate-700 dark:text-slate-300 text-sm font-bold mb-2">
              Content
            </label>
            <textarea
              value={formData.content}
              onChange={(e) =>
                setFormData({ ...formData, content: e.target.value })
              }
              className="shadow appearance-none border border-slate-300 dark:border-slate-700 rounded w-full py-2 px-3 text-slate-900 dark:text-white bg-white dark:bg-slate-800 leading-tight focus:outline-none focus:shadow-outline focus:border-blue-500"
              rows={5}
              required
            />
          </div>
          <div className="flex gap-2">
            <button
              type="submit"
              className="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded shadow-lg transition-colors"
            >
              {editingNotice ? 'Update' : 'Create'}
            </button>
            <button
              type="button"
              onClick={cancelEdit}
              className="bg-slate-500 hover:bg-slate-600 text-white px-4 py-2 rounded shadow-lg transition-colors"
            >
              Cancel
            </button>
          </div>
        </form>
      )}

      <div className="space-y-4">
        {notices.length === 0 ? (
          <div className="text-center py-8 text-slate-500 dark:text-slate-400">
            No notices yet
          </div>
        ) : (
          notices.map((notice) => (
            <div
              key={notice.id}
              className="bg-white dark:bg-slate-900 shadow-md rounded-lg px-8 pt-6 pb-8 border border-slate-200 dark:border-slate-800"
            >
              <div className="flex justify-between items-start mb-4">
                <div className="flex-1">
                  <h2 className="text-2xl font-bold mb-2 text-slate-900 dark:text-white">
                    {notice.title}
                  </h2>
                  <div className="flex items-center gap-2 text-sm text-slate-600 dark:text-slate-400 mb-2">
                    <span>By {notice.author.username}</span>
                    <span>•</span>
                    <span>
                      {new Date(notice.createdAt).toLocaleDateString()}
                    </span>
                  </div>
                </div>
                {isAdmin && (
                  <div className="flex gap-2">
                    <button
                      onClick={() => startEdit(notice)}
                      className="bg-yellow-500 hover:bg-yellow-600 text-white px-3 py-1 rounded text-sm shadow transition-colors"
                    >
                      Edit
                    </button>
                    <button
                      onClick={() => handleDelete(notice.id)}
                      className="bg-red-500 hover:bg-red-600 text-white px-3 py-1 rounded text-sm shadow transition-colors"
                    >
                      Delete
                    </button>
                  </div>
                )}
              </div>
              <p className="text-slate-700 dark:text-slate-300 whitespace-pre-wrap">
                {notice.content}
              </p>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
