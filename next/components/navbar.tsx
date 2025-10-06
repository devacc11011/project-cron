'use client';

import Link from 'next/link';
import { AuthButton } from './auth-button';
import { UsageBadge } from './usage-badge';
import { Terminal } from 'lucide-react';

export function Navbar() {
  return (
    <nav className="bg-black border-b border-zinc-800 backdrop-blur-sm bg-opacity-90 sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          <div className="flex items-center gap-8">
            <Link
              href="/"
              className="flex items-center gap-2 text-xl font-bold text-white hover:text-blue-400 transition-colors"
            >
              <Terminal className="w-6 h-6" />
              <span className="bg-gradient-to-r from-blue-400 to-purple-500 bg-clip-text text-transparent">
                Project Cron
              </span>
            </Link>
            <div className="hidden md:flex gap-1">
              <Link
                href="/notices"
                className="px-4 py-2 text-zinc-400 hover:text-white hover:bg-zinc-800 rounded-lg transition-all duration-200"
              >
                Notices
              </Link>
              <Link
                href="/schedules"
                className="px-4 py-2 text-zinc-400 hover:text-white hover:bg-zinc-800 rounded-lg transition-all duration-200"
              >
                Schedules
              </Link>
              <Link
                href="/schedules/history"
                className="px-4 py-2 text-zinc-400 hover:text-white hover:bg-zinc-800 rounded-lg transition-all duration-200"
              >
                History
              </Link>
              <Link
                href="/tasks"
                className="px-4 py-2 text-zinc-400 hover:text-white hover:bg-zinc-800 rounded-lg transition-all duration-200"
              >
                Tasks
              </Link>
            </div>
          </div>
          <div className="flex items-center gap-3">
            <UsageBadge />
            <AuthButton />
          </div>
        </div>
      </div>
    </nav>
  );
}
