import { Clock, Sparkles, Calendar, Zap } from "lucide-react";
import { HealthIndicator } from "@/components/health-indicator";
import { Navbar } from "@/components/navbar";

export default function Home() {
  return (
    <div className="min-h-screen bg-black">
      <HealthIndicator />
      <Navbar />

      {/* Gradient overlay */}
      <div className="absolute inset-0 bg-gradient-to-br from-blue-500/10 via-purple-500/10 to-pink-500/10 pointer-events-none"></div>

      <div className="relative container mx-auto px-4 py-16">
        {/* Hero Section */}
        <div className="flex flex-col items-center text-center space-y-8 max-w-4xl mx-auto pt-20">
          <div className="inline-flex items-center gap-2 bg-zinc-900 border border-zinc-800 px-4 py-2 rounded-full">
            <Sparkles className="w-4 h-4 text-blue-400" />
            <span className="text-sm font-medium bg-gradient-to-r from-blue-400 to-purple-400 bg-clip-text text-transparent">
              AI Automation Scheduler
            </span>
          </div>

          <h1 className="text-5xl md:text-7xl font-bold tracking-tight text-white">
            Set Your Schedule,
            <br />
            <span className="bg-gradient-to-r from-blue-400 via-purple-400 to-pink-400 bg-clip-text text-transparent">
              AI Does the Rest
            </span>
          </h1>

          <p className="text-xl text-zinc-400 max-w-2xl leading-relaxed">
            Delegate tasks to AI at your preferred time.
            A smart scheduler that runs automatically.
          </p>

          <div className="flex flex-col sm:flex-row gap-4 pt-4">
            <button className="px-8 py-4 bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-500 hover:to-purple-500 text-white rounded-lg font-semibold transition-all duration-200 shadow-lg shadow-blue-500/20 hover:shadow-blue-500/40">
              Get Started
            </button>
            <button className="px-8 py-4 bg-zinc-900 hover:bg-zinc-800 text-white rounded-lg font-semibold border border-zinc-800 hover:border-zinc-700 transition-all duration-200">
              Learn More
            </button>
          </div>
        </div>

        {/* Features */}
        <div className="grid md:grid-cols-3 gap-6 max-w-6xl mx-auto mt-32">
          <div className="group p-8 bg-zinc-900 rounded-2xl border border-zinc-800 hover:border-blue-500/50 transition-all duration-300 hover:shadow-lg hover:shadow-blue-500/10">
            <div className="w-14 h-14 bg-gradient-to-br from-blue-500/20 to-blue-600/20 rounded-xl flex items-center justify-center mb-6 group-hover:scale-110 transition-transform duration-300">
              <Clock className="w-7 h-7 text-blue-400" />
            </div>
            <h3 className="text-xl font-semibold text-white mb-3">
              Schedule Setup
            </h3>
            <p className="text-zinc-400 leading-relaxed">
              Set up schedules to automatically execute tasks at your desired time
            </p>
          </div>

          <div className="group p-8 bg-zinc-900 rounded-2xl border border-zinc-800 hover:border-purple-500/50 transition-all duration-300 hover:shadow-lg hover:shadow-purple-500/10">
            <div className="w-14 h-14 bg-gradient-to-br from-purple-500/20 to-purple-600/20 rounded-xl flex items-center justify-center mb-6 group-hover:scale-110 transition-transform duration-300">
              <Zap className="w-7 h-7 text-purple-400" />
            </div>
            <h3 className="text-xl font-semibold text-white mb-3">
              AI Auto-Execution
            </h3>
            <p className="text-zinc-400 leading-relaxed">
              AI automatically performs tasks at the scheduled time
            </p>
          </div>

          <div className="group p-8 bg-zinc-900 rounded-2xl border border-zinc-800 hover:border-pink-500/50 transition-all duration-300 hover:shadow-lg hover:shadow-pink-500/10">
            <div className="w-14 h-14 bg-gradient-to-br from-pink-500/20 to-pink-600/20 rounded-xl flex items-center justify-center mb-6 group-hover:scale-110 transition-transform duration-300">
              <Calendar className="w-7 h-7 text-pink-400" />
            </div>
            <h3 className="text-xl font-semibold text-white mb-3">
              Recurring Task Management
            </h3>
            <p className="text-zinc-400 leading-relaxed">
              Easily manage everything from one-time to recurring tasks
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
