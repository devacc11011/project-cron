'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import TaskList from '@/components/task-list';
import { api, User } from '@/lib/api';
import { Loader2, ShieldAlert } from 'lucide-react';

export default function TasksPage() {
  const router = useRouter();
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const checkAdmin = async () => {
      const currentUser = await api.getCurrentUser();

      if (!currentUser) {
        router.push('/');
        return;
      }

      if (!currentUser.roles.includes('ROLE_ADMIN')) {
        setUser(currentUser);
        setLoading(false);
        return;
      }

      setUser(currentUser);
      setLoading(false);
    };

    checkAdmin();
  }, [router]);

  if (loading) {
    return (
      <div className="min-h-screen bg-black flex items-center justify-center">
        <Loader2 className="w-8 h-8 text-blue-400 animate-spin" />
      </div>
    );
  }

  if (user && !user.roles.includes('ROLE_ADMIN')) {
    return (
      <div className="min-h-screen bg-black flex items-center justify-center">
        <div className="max-w-md mx-auto px-4">
          <div className="bg-zinc-900 border border-red-800 rounded-lg p-8 text-center">
            <ShieldAlert className="w-16 h-16 text-red-400 mx-auto mb-4" />
            <h1 className="text-2xl font-bold text-white mb-2">Access Denied</h1>
            <p className="text-gray-400 mb-6">
              This page is restricted to administrators only.
            </p>
            <button
              onClick={() => router.push('/')}
              className="px-6 py-2 bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-500 hover:to-purple-500 text-white font-semibold rounded-lg transition"
            >
              Go to Home
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-black">
      <div className="max-w-6xl mx-auto px-4 py-12">
        <div className="mb-8">
          <div className="flex items-center gap-3 mb-3">
            <h1 className="text-4xl font-bold bg-gradient-to-r from-blue-400 via-purple-500 to-pink-500 bg-clip-text text-transparent">
              AI Tasks
            </h1>
            <span className="text-xs px-3 py-1 bg-gradient-to-r from-red-500 to-orange-500 rounded-full text-white font-bold">
              ADMIN
            </span>
          </div>
          <p className="text-gray-400">
            Create and manage AI-powered tasks for testing
          </p>
        </div>
        <TaskList />
      </div>
    </div>
  );
}
