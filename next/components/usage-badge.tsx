'use client';

import { useEffect, useState } from 'react';
import { api, UserTokenUsage } from '@/lib/api';
import { Activity } from 'lucide-react';

export function UsageBadge() {
  const [usage, setUsage] = useState<UserTokenUsage | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadUsage = async () => {
      try {
        const data = await api.getCurrentUsage();
        setUsage(data);
      } catch (error) {
        console.error('Failed to load usage:', error);
      } finally {
        setLoading(false);
      }
    };

    loadUsage();
  }, []);

  if (loading || !usage) {
    return null;
  }

  const getUsageColor = (percentage: number) => {
    if (percentage >= 90) return 'text-red-400 bg-red-500/10 border-red-500/20';
    if (percentage >= 75) return 'text-yellow-400 bg-yellow-500/10 border-yellow-500/20';
    return 'text-green-400 bg-green-500/10 border-green-500/20';
  };

  return (
    <div className={`flex items-center gap-2 px-3 py-1.5 rounded-lg border ${getUsageColor(usage.usagePercentage)}`}>
      <Activity className="w-4 h-4" />
      <div className="text-xs font-medium">
        <span>{usage.remainingTokens.toLocaleString()}</span>
        <span className="opacity-70"> / {usage.tokenLimit.toLocaleString()} tokens</span>
      </div>
    </div>
  );
}
