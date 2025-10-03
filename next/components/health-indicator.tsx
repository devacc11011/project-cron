"use client";

import { useEffect, useState } from "react";
import { Activity } from "lucide-react";

export function HealthIndicator() {
  const [isHealthy, setIsHealthy] = useState<boolean | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const checkHealth = async () => {
      try {
        const response = await fetch("http://localhost:8080/api/health");
        setIsHealthy(response.ok);
      } catch {
        setIsHealthy(false);
      } finally {
        setIsLoading(false);
      }
    };

    checkHealth();
    const interval = setInterval(checkHealth, 30000); // Check every 30 seconds

    return () => clearInterval(interval);
  }, []);

  if (isLoading) {
    return (
      <div className="fixed top-4 right-4 flex items-center gap-2 px-4 py-2 bg-slate-100 dark:bg-slate-800 rounded-full border border-slate-200 dark:border-slate-700">
        <Activity className="w-4 h-4 text-slate-400 animate-pulse" />
        <span className="text-sm text-slate-600 dark:text-slate-400">Checking...</span>
      </div>
    );
  }

  return (
    <div
      className={`fixed top-4 right-4 flex items-center gap-2 px-4 py-2 rounded-full border ${
        isHealthy
          ? "bg-green-50 dark:bg-green-900/20 border-green-200 dark:border-green-800"
          : "bg-red-50 dark:bg-red-900/20 border-red-200 dark:border-red-800"
      }`}
    >
      <div
        className={`w-2 h-2 rounded-full ${
          isHealthy ? "bg-green-500" : "bg-red-500"
        } ${isHealthy ? "animate-pulse" : ""}`}
      />
      <Activity
        className={`w-4 h-4 ${
          isHealthy
            ? "text-green-600 dark:text-green-400"
            : "text-red-600 dark:text-red-400"
        }`}
      />
      <span
        className={`text-sm font-medium ${
          isHealthy
            ? "text-green-700 dark:text-green-300"
            : "text-red-700 dark:text-red-300"
        }`}
      >
        Backend {isHealthy ? "Online" : "Offline"}
      </span>
    </div>
  );
}
