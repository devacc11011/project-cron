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

export interface Notice {
  id: number;
  title: string;
  content: string;
  author: {
    discordId: string;
    username: string;
    avatarUrl: string | null;
  };
  createdAt: string;
  updatedAt: string;
}

export interface NoticeRequest {
  title: string;
  content: string;
}

export interface Task {
  id: number;
  title: string;
  prompt: string;
  result: string | null;
  status: string;
  aiProvider: string;
  enableWebSearch: boolean;
  user: {
    discordId: string;
    username: string;
  };
  createdAt: string;
  updatedAt: string;
  executedAt: string | null;
}

export interface TaskRequest {
  title: string;
  prompt: string;
  aiProvider?: string;
  enableWebSearch?: boolean;
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

  // Notice APIs
  async getAllNotices(): Promise<Notice[]> {
    try {
      const response = await fetch(`${API_URL}/api/notices`, {
        credentials: 'include',
      });

      if (!response.ok) {
        throw new Error('Failed to fetch notices');
      }

      return response.json();
    } catch (error) {
      console.error('Failed to fetch notices:', error);
      throw error;
    }
  },

  async getNoticeById(id: number): Promise<Notice> {
    try {
      const response = await fetch(`${API_URL}/api/notices/${id}`, {
        credentials: 'include',
      });

      if (!response.ok) {
        throw new Error('Failed to fetch notice');
      }

      return response.json();
    } catch (error) {
      console.error('Failed to fetch notice:', error);
      throw error;
    }
  },

  async createNotice(notice: NoticeRequest): Promise<Notice> {
    try {
      const response = await fetch(`${API_URL}/api/notices`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify(notice),
      });

      if (!response.ok) {
        throw new Error('Failed to create notice');
      }

      return response.json();
    } catch (error) {
      console.error('Failed to create notice:', error);
      throw error;
    }
  },

  async updateNotice(id: number, notice: NoticeRequest): Promise<Notice> {
    try {
      const response = await fetch(`${API_URL}/api/notices/${id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify(notice),
      });

      if (!response.ok) {
        throw new Error('Failed to update notice');
      }

      return response.json();
    } catch (error) {
      console.error('Failed to update notice:', error);
      throw error;
    }
  },

  async deleteNotice(id: number): Promise<void> {
    try {
      const response = await fetch(`${API_URL}/api/notices/${id}`, {
        method: 'DELETE',
        credentials: 'include',
      });

      if (!response.ok) {
        throw new Error('Failed to delete notice');
      }
    } catch (error) {
      console.error('Failed to delete notice:', error);
      throw error;
    }
  },

  // Task APIs
  async getAllTasks(): Promise<Task[]> {
    try {
      const response = await fetch(`${API_URL}/api/tasks`, {
        credentials: 'include',
      });

      if (!response.ok) {
        throw new Error('Failed to fetch tasks');
      }

      return response.json();
    } catch (error) {
      console.error('Failed to fetch tasks:', error);
      throw error;
    }
  },

  async getTaskById(id: number): Promise<Task> {
    try {
      const response = await fetch(`${API_URL}/api/tasks/${id}`, {
        credentials: 'include',
      });

      if (!response.ok) {
        throw new Error('Failed to fetch task');
      }

      return response.json();
    } catch (error) {
      console.error('Failed to fetch task:', error);
      throw error;
    }
  },

  async createTask(task: TaskRequest): Promise<Task> {
    try {
      const response = await fetch(`${API_URL}/api/tasks`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify(task),
      });

      if (!response.ok) {
        const errorText = await response.text();
        console.error('Create task failed:', response.status, errorText);
        throw new Error(`Failed to create task: ${response.status} ${errorText}`);
      }

      return response.json();
    } catch (error) {
      console.error('Failed to create task:', error);
      throw error;
    }
  },

  async executeTask(id: number): Promise<Task> {
    try {
      const response = await fetch(`${API_URL}/api/tasks/${id}/execute`, {
        method: 'POST',
        credentials: 'include',
      });

      if (!response.ok) {
        throw new Error('Failed to execute task');
      }

      return response.json();
    } catch (error) {
      console.error('Failed to execute task:', error);
      throw error;
    }
  },

  async deleteTask(id: number): Promise<void> {
    try {
      const response = await fetch(`${API_URL}/api/tasks/${id}`, {
        method: 'DELETE',
        credentials: 'include',
      });

      if (!response.ok) {
        throw new Error('Failed to delete task');
      }
    } catch (error) {
      console.error('Failed to delete task:', error);
      throw error;
    }
  },
};
