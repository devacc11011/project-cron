import TaskList from '@/components/task-list';

export default function TasksPage() {
  return (
    <div className="min-h-screen bg-black">
      <div className="max-w-6xl mx-auto px-4 py-12">
        <div className="mb-8">
          <h1 className="text-4xl font-bold mb-3 bg-gradient-to-r from-blue-400 via-purple-500 to-pink-500 bg-clip-text text-transparent">
            AI Tasks
          </h1>
          <p className="text-gray-400">
            Create and manage AI-powered tasks using Gemini API
          </p>
        </div>
        <TaskList />
      </div>
    </div>
  );
}
