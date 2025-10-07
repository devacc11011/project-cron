'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { api, User, UserTokenUsage } from '@/lib/api';

export default function AdminPage() {
  const router = useRouter();
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [users, setUsers] = useState<User[]>([]);
  const [tokenUsages, setTokenUsages] = useState<UserTokenUsage[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const initialize = async () => {
      try {
        const user = await api.getCurrentUser();
        if (!user || !user.roles.includes('ADMIN')) {
          router.push('/');
          return;
        }
        setCurrentUser(user);

        const [usersData, tokensData] = await Promise.all([
          api.getAllUsersForAdmin(),
          api.getAllTokenUsagesForAdmin(),
        ]);

        setUsers(usersData);
        setTokenUsages(tokensData);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'An unknown error occurred');
        // Optionally redirect on error as well
        // router.push('/'); 
      } finally {
        setLoading(false);
      }
    };

    initialize();
  }, [router]);

  if (loading) {
    return <div className="flex justify-center items-center h-screen">Loading...</div>;
  }

  if (error) {
    return <div className="flex justify-center items-center h-screen text-red-500">Error: {error}</div>;
  }

  if (!currentUser) {
    // This case is mostly handled by the redirect, but as a fallback
    return null;
  }

  return (
    <div className="container mx-auto p-4">
      <h1 className="text-2xl font-bold mb-4">Admin Dashboard</h1>

      <section>
        <h2 className="text-xl font-semibold mb-2">Users ({users.length})</h2>
        <div className="overflow-x-auto bg-white rounded-lg shadow">
          <table className="min-w-full border-collapse">
            <thead className="bg-gray-50">
              <tr>
                <th className="py-3 px-4 border-b text-left">ID</th>
                <th className="py-3 px-4 border-b text-left">Username</th>
                <th className="py-3 px-4 border-b text-left">Roles</th>
                <th className="py-3 px-4 border-b text-left">Created At</th>
              </tr>
            </thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.id} className="hover:bg-gray-100">
                  <td className="py-2 px-4 border-b">{u.id}</td>
                  <td className="py-2 px-4 border-b flex items-center">
                    {u.avatarUrl && (
                      // eslint-disable-next-line @next/next/no-img-element
                      <img src={u.avatarUrl} alt={u.username} className="w-8 h-8 rounded-full mr-3" />
                    )}
                    <span>{u.username}</span>
                  </td>
                  <td className="py-2 px-4 border-b">{u.roles.join(', ')}</td>
                  <td className="py-2 px-4 border-b">{new Date(u.createdAt).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      <section className="mt-8">
        <h2 className="text-xl font-semibold mb-2">Token Usage</h2>
        <div className="overflow-x-auto bg-white rounded-lg shadow">
          <table className="min-w-full border-collapse">
            <thead className="bg-gray-50">
              <tr>
                <th className="py-3 px-4 border-b text-left">User</th>
                <th className="py-3 px-4 border-b text-left">Year-Month</th>
                <th className="py-3 px-4 border-b text-left">Used / Limit</th>
                <th className="py-3 px-4 border-b text-left">Usage</th>
              </tr>
            </thead>
            <tbody>
              {tokenUsages.map((t, index) => (
                <tr key={index} className="hover:bg-gray-100">
                  <td className="py-2 px-4 border-b">{t.username}</td>
                  <td className="py-2 px-4 border-b">{t.yearMonth}</td>
                  <td className="py-2 px-4 border-b">
                    {t.totalTokensUsed.toLocaleString()} / {t.tokenLimit.toLocaleString()}
                  </td>
                  <td className="py-2 px-4 border-b">
                    <div className="w-full bg-gray-200 rounded-full h-2.5">
                      <div
                        className="bg-blue-600 h-2.5 rounded-full"
                        style={{ width: `${t.usagePercentage}%` }}
                      ></div>
                    </div>
                    <span className="text-xs font-medium text-gray-700">{t.usagePercentage?.toFixed(2)}%</span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}
