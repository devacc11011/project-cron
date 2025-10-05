const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

export interface User {
  id: number;
  discordId: string;
  username: string;
  email: string | null;
  avatarUrl: string | null;
  roles: string[];
  createdAt: string;
  updatedAt: string;
}

export const api = {
  async getCurrentUser(): Promise<User | null> {
    try {
      const response = await fetch(`${API_URL}/api/auth/me`, {
        credentials: 'include',
      });

      if (!response.ok) {
        return null;
      }

      return response.json();
    } catch (error) {
      console.error('Failed to fetch current user:', error);
      return null;
    }
  },

  async checkAuthStatus(): Promise<boolean> {
    try {
      const response = await fetch(`${API_URL}/api/auth/status`, {
        credentials: 'include',
      });

      if (!response.ok) {
        return false;
      }

      return response.json();
    } catch (error) {
      console.error('Failed to check auth status:', error);
      return false;
    }
  },

  getDiscordLoginUrl(): string {
    return `${API_URL}/oauth2/authorization/discord`;
  },
};
