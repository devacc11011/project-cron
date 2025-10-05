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
    <div className="max-w-5xl mx-auto p-6">
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-4xl font-bold text-white">
          Notices
        </h1>
        {isAdmin && !isCreating && !editingNotice && (
          <button
            onClick={() => setIsCreating(true)}
            className="bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-500 hover:to-purple-500 text-white px-6 py-3 rounded-lg font-medium shadow-lg shadow-blue-500/20 hover:shadow-blue-500/40 transition-all duration-200"
          >
            Create Notice
          </button>
        )}
      </div>

      {(isCreating || editingNotice) && (
        <form
          onSubmit={editingNotice ? handleUpdate : handleCreate}
          className="bg-zinc-900 shadow-xl rounded-2xl px-8 pt-8 pb-8 mb-8 border border-zinc-800"
        >
          <h2 className="text-2xl font-bold mb-6 text-white">
            {editingNotice ? 'Edit Notice' : 'Create Notice'}
          </h2>
          <div className="mb-6">
            <label className="block text-zinc-300 text-sm font-semibold mb-2">
              Title
            </label>
            <input
              type="text"
              value={formData.title}
              onChange={(e) =>
                setFormData({ ...formData, title: e.target.value })
              }
              className="w-full py-3 px-4 text-white bg-black border border-zinc-800 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/50 focus:border-blue-500 transition-all"
              required
            />
          </div>
          <div className="mb-6">
            <label className="block text-zinc-300 text-sm font-semibold mb-2">
              Content
            </label>
            <textarea
              value={formData.content}
              onChange={(e) =>
                setFormData({ ...formData, content: e.target.value })
              }
              className="w-full py-3 px-4 text-white bg-black border border-zinc-800 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/50 focus:border-blue-500 transition-all resize-none"
              rows={8}
              required
            />
          </div>
          <div className="flex gap-3">
            <button
              type="submit"
              className="bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-500 hover:to-purple-500 text-white px-6 py-3 rounded-lg font-medium shadow-lg shadow-blue-500/20 hover:shadow-blue-500/40 transition-all duration-200"
            >
              {editingNotice ? 'Update' : 'Create'}
            </button>
            <button
              type="button"
              onClick={cancelEdit}
              className="bg-zinc-800 hover:bg-zinc-700 text-zinc-300 hover:text-white px-6 py-3 rounded-lg font-medium transition-all duration-200"
            >
              Cancel
            </button>
          </div>
        </form>
      )}

      <div className="space-y-6">
        {notices.length === 0 ? (
          <div className="text-center py-16 text-zinc-500">
            No notices yet
          </div>
        ) : (
          notices.map((notice) => (
            <div
              key={notice.id}
              className="group bg-zinc-900 shadow-xl rounded-2xl px-8 pt-6 pb-8 border border-zinc-800 hover:border-zinc-700 transition-all duration-300"
            >
              <div className="flex justify-between items-start mb-4">
                <div className="flex-1">
                  <h2 className="text-2xl font-bold mb-3 text-white group-hover:text-blue-400 transition-colors">
                    {notice.title}
                  </h2>
                  <div className="flex items-center gap-3 text-sm text-zinc-500 mb-4">
                    <span className="flex items-center gap-1">
                      <span className="text-zinc-400">By</span>
                      <span className="text-blue-400 font-medium">{notice.author.username}</span>
                    </span>
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
                      className="bg-yellow-600 hover:bg-yellow-500 text-white px-4 py-2 rounded-lg text-sm font-medium shadow transition-all duration-200"
                    >
                      Edit
                    </button>
                    <button
                      onClick={() => handleDelete(notice.id)}
                      className="bg-red-600 hover:bg-red-500 text-white px-4 py-2 rounded-lg text-sm font-medium shadow transition-all duration-200"
                    >
                      Delete
                    </button>
                  </div>
                )}
              </div>
              <p className="text-zinc-300 whitespace-pre-wrap leading-relaxed">
                {notice.content}
              </p>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
