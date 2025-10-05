'use client';

import Link from 'next/link';
import { AuthButton } from './auth-button';

export function Navbar() {
  return (
    <nav className="bg-white dark:bg-slate-900 border-b border-slate-200 dark:border-slate-800">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          <div className="flex items-center gap-8">
            <Link
              href="/"
              className="text-xl font-bold text-slate-900 dark:text-white"
            >
              Project Cron
            </Link>
            <div className="flex gap-4">
              <Link
                href="/notices"
                className="text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white transition-colors"
              >
                Notices
              </Link>
            </div>
          </div>
          <AuthButton />
        </div>
      </div>
    </nav>
  );
}
