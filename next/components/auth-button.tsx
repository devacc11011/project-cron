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
      <div className="h-10 w-32 bg-zinc-800 rounded-lg animate-pulse"></div>
    );
  }

  if (!user) {
    return (
      <button
        onClick={handleLogin}
        className="flex items-center gap-2 px-5 py-2.5 bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-500 hover:to-purple-500 text-white rounded-lg font-medium transition-all duration-200 shadow-lg shadow-blue-500/20"
      >
        <LogIn className="w-4 h-4" />
        Login with Discord
      </button>
    );
  }

  return (
    <div className="flex items-center gap-3">
      <div className="flex items-center gap-2 px-4 py-2 bg-zinc-900 rounded-lg border border-zinc-800">
        {user.avatarUrl ? (
          <Image
            src={user.avatarUrl}
            alt={user.username}
            width={24}
            height={24}
            className="w-6 h-6 rounded-full ring-2 ring-blue-500/20"
          />
        ) : (
          <UserIcon className="w-6 h-6 text-zinc-400" />
        )}
        <span className="text-sm font-medium text-white">
          {user.username}
        </span>
      </div>
      <button
        onClick={handleLogout}
        className="flex items-center gap-2 px-4 py-2 bg-zinc-800 hover:bg-zinc-700 text-zinc-300 hover:text-white rounded-lg font-medium transition-all duration-200"
      >
        <LogOut className="w-4 h-4" />
        Logout
      </button>
    </div>
  );
}
