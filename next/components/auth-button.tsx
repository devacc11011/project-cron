'use client';

import { useState, useEffect } from 'react';
import Image from 'next/image';
import { LogIn, LogOut, User as UserIcon } from 'lucide-react';
import { api, type User } from '@/lib/api';

export function AuthButton() {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    checkAuth();
  }, []);

  const checkAuth = async () => {
    setLoading(true);
    const currentUser = await api.getCurrentUser();
    setUser(currentUser);
    setLoading(false);
  };

  const handleLogin = () => {
    window.location.href = api.getDiscordLoginUrl();
  };

  const handleLogout = async () => {
    // Logout logic will be implemented later
    setUser(null);
  };

  if (loading) {
    return (
      <div className="h-10 w-32 bg-slate-200 dark:bg-slate-800 rounded-lg animate-pulse"></div>
    );
  }

  if (!user) {
    return (
      <button
        onClick={handleLogin}
        className="flex items-center gap-2 px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg font-medium transition-colors"
      >
        <LogIn className="w-4 h-4" />
        Login with Discord
      </button>
    );
  }

  return (
    <div className="flex items-center gap-3">
      <div className="flex items-center gap-2 px-4 py-2 bg-white dark:bg-slate-800 rounded-lg border border-slate-200 dark:border-slate-700">
        {user.avatarUrl ? (
          <Image
            src={user.avatarUrl}
            alt={user.username}
            width={24}
            height={24}
            className="w-6 h-6 rounded-full"
          />
        ) : (
          <UserIcon className="w-6 h-6 text-slate-500" />
        )}
        <span className="text-sm font-medium text-slate-900 dark:text-white">
          {user.username}
        </span>
      </div>
      <button
        onClick={handleLogout}
        className="flex items-center gap-2 px-4 py-2 bg-slate-200 hover:bg-slate-300 dark:bg-slate-700 dark:hover:bg-slate-600 text-slate-900 dark:text-white rounded-lg font-medium transition-colors"
      >
        <LogOut className="w-4 h-4" />
        Logout
      </button>
    </div>
  );
}
