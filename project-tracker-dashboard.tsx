import React, { useState } from 'react';
import { BarChart, Bar, LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts';
import { TrendingUp, Users, CheckCircle, Clock, Target, Activity } from 'lucide-react';

export default function ProjectTrackerDashboard() {
  const [timeframe, setTimeframe] = useState('week');

  // Project data
  const projects = [
    { id: 1, name: 'Website Redesign', progress: 75, status: 'on-track', tasks: 24, completed: 18 },
    { id: 2, name: 'Mobile App Launch', progress: 45, status: 'on-track', tasks: 32, completed: 14 },
    { id: 3, name: 'Marketing Campaign', progress: 90, status: 'on-track', tasks: 15, completed: 13 },
    { id: 4, name: 'API Integration', progress: 30, status: 'at-risk', tasks: 20, completed: 6 },
  ];

  // Improvement data over time
  const improvementData = [
    { period: 'Week 1', completed: 12, velocity: 2.4 },
    { period: 'Week 2', completed: 18, velocity: 3.6 },
    { period: 'Week 3', completed: 25, velocity: 5.0 },
    { period: 'Week 4', completed: 31, velocity: 6.2 },
    { period: 'Week 5', completed: 38, velocity: 7.6 },
    { period: 'Week 6', completed: 51, velocity: 10.2 },
  ];

  // User statistics
  const userStats = {
    totalUsers: 24,
    activeToday: 18,
    avgTasksPerUser: 4.2,
    topPerformer: 'Sarah Chen',
  };

  // User activity distribution
  const userActivityData = [
    { name: 'Highly Active', value: 8, color: '#10b981' },
    { name: 'Active', value: 10, color: '#3b82f6' },
    { name: 'Moderate', value: 4, color: '#f59e0b' },
    { name: 'Inactive', value: 2, color: '#ef4444' },
  ];

  // Task completion by category
  const tasksByCategory = [
    { category: 'Design', completed: 24, pending: 8 },
    { category: 'Development', completed: 32, pending: 15 },
    { category: 'Testing', completed: 15, pending: 12 },
    { category: 'Documentation', completed: 10, pending: 5 },
  ];

  const StatCard = ({ icon: Icon, title, value, subtitle, trend }) => (
    <div className="bg-gradient-to-br from-white to-gray-50 rounded-xl p-6 shadow-lg border border-purple-100 hover:shadow-xl transition-shadow duration-300">
      <div className="flex items-start justify-between">
        <div>
          <p className="text-sm text-gray-600 mb-1">{title}</p>
          <p className="text-3xl font-bold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent">{value}</p>
          {subtitle && <p className="text-sm text-gray-500 mt-1">{subtitle}</p>}
        </div>
        <div className="bg-gradient-to-br from-indigo-500 to-purple-500 p-3 rounded-lg shadow-md">
          <Icon className="w-6 h-6 text-white" />
        </div>
      </div>
      {trend && (
        <div className="flex items-center mt-4 text-sm">
          <TrendingUp className="w-4 h-4 text-green-500 mr-1" />
          <span className="text-green-600 font-medium">{trend}</span>
          <span className="text-gray-500 ml-1">vs last period</span>
        </div>
      )}
    </div>
  );

  return (
    <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-purple-50 to-pink-50 p-8">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-4xl font-bold bg-gradient-to-r from-indigo-600 via-purple-600 to-pink-600 bg-clip-text text-transparent mb-2">Project Dashboard</h1>
          <p className="text-gray-700">Track your projects and team performance</p>
        </div>

        {/* Stats Overview */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <StatCard
            icon={Target}
            title="Active Projects"
            value={projects.length}
            subtitle="All on schedule"
            trend="+2 this month"
          />
          <StatCard
            icon={CheckCircle}
            title="Tasks Completed"
            value="51"
            subtitle="This week"
            trend="+34%"
          />
          <StatCard
            icon={Users}
            title="Active Users"
            value={userStats.activeToday}
            subtitle={`of ${userStats.totalUsers} total`}
            trend="+12%"
          />
          <StatCard
            icon={Activity}
            title="Avg Velocity"
            value="10.2"
            subtitle="Tasks per week"
            trend="+64%"
          />
        </div>

        {/* Projects Progress */}
        <div className="bg-gradient-to-br from-white to-indigo-50 rounded-xl shadow-lg border border-indigo-100 p-6 mb-8">
          <h2 className="text-xl font-bold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent mb-6">Project Progress</h2>
          <div className="space-y-6">
            {projects.map((project) => (
              <div key={project.id} className="space-y-2">
                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="font-semibold text-gray-900">{project.name}</h3>
                    <p className="text-sm text-gray-500">
                      {project.completed} of {project.tasks} tasks completed
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="text-2xl font-bold bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent">{project.progress}%</p>
                    <span
                      className={`text-xs px-2 py-1 rounded-full ${
                        project.status === 'on-track'
                          ? 'bg-green-100 text-green-700'
                          : 'bg-orange-100 text-orange-700'
                      }`}
                    >
                      {project.status === 'on-track' ? 'On Track' : 'At Risk'}
                    </span>
                  </div>
                </div>
                <div className="w-full bg-gray-200 rounded-full h-3">
                  <div
                    className={`h-3 rounded-full transition-all duration-500 ${
                      project.status === 'on-track' ? 'bg-gradient-to-r from-indigo-500 via-purple-500 to-pink-500' : 'bg-gradient-to-r from-orange-500 to-red-500'
                    }`}
                    style={{ width: `${project.progress}%` }}
                  />
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Improvement Metrics */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
          <div className="bg-gradient-to-br from-white to-blue-50 rounded-xl shadow-lg border border-blue-100 p-6">
            <h2 className="text-xl font-bold bg-gradient-to-r from-blue-600 to-cyan-600 bg-clip-text text-transparent mb-6">Improvement Trend</h2>
            <ResponsiveContainer width="100%" height={300}>
              <LineChart data={improvementData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                <XAxis dataKey="period" stroke="#6b7280" />
                <YAxis stroke="#6b7280" />
                <Tooltip
                  contentStyle={{
                    backgroundColor: '#fff',
                    border: '1px solid #e5e7eb',
                    borderRadius: '8px',
                  }}
                />
                <Line
                  type="monotone"
                  dataKey="completed"
                  stroke="#6366f1"
                  strokeWidth={3}
                  dot={{ fill: '#6366f1', r: 5 }}
                  name="Tasks Completed"
                />
                <Line
                  type="monotone"
                  dataKey="velocity"
                  stroke="#14b8a6"
                  strokeWidth={3}
                  dot={{ fill: '#14b8a6', r: 5 }}
                  name="Velocity"
                />
              </LineChart>
            </ResponsiveContainer>
          </div>

          <div className="bg-gradient-to-br from-white to-purple-50 rounded-xl shadow-lg border border-purple-100 p-6">
            <h2 className="text-xl font-bold bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent mb-6">Tasks by Category</h2>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={tasksByCategory}>
                <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                <XAxis dataKey="category" stroke="#6b7280" />
                <YAxis stroke="#6b7280" />
                <Tooltip
                  contentStyle={{
                    backgroundColor: '#fff',
                    border: '1px solid #e5e7eb',
                    borderRadius: '8px',
                  }}
                />
                <Bar dataKey="completed" fill="url(#colorCompleted)" radius={[8, 8, 0, 0]} name="Completed" />
                <Bar dataKey="pending" fill="#e5e7eb" radius={[8, 8, 0, 0]} name="Pending" />
                <defs>
                  <linearGradient id="colorCompleted" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stopColor="#a855f7" stopOpacity={1}/>
                    <stop offset="100%" stopColor="#ec4899" stopOpacity={1}/>
                  </linearGradient>
                </defs>
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* User Statistics */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          <div className="bg-gradient-to-br from-white to-teal-50 rounded-xl shadow-lg border border-teal-100 p-6">
            <h2 className="text-xl font-bold bg-gradient-to-r from-teal-600 to-cyan-600 bg-clip-text text-transparent mb-6">User Activity</h2>
            <ResponsiveContainer width="100%" height={250}>
              <PieChart>
                <Pie
                  data={userActivityData}
                  cx="50%"
                  cy="50%"
                  innerRadius={60}
                  outerRadius={90}
                  paddingAngle={5}
                  dataKey="value"
                >
                  {userActivityData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
            <div className="space-y-2 mt-4">
              {userActivityData.map((item) => (
                <div key={item.name} className="flex items-center justify-between">
                  <div className="flex items-center">
                    <div
                      className="w-3 h-3 rounded-full mr-2"
                      style={{ backgroundColor: item.color }}
                    />
                    <span className="text-sm text-gray-600">{item.name}</span>
                  </div>
                  <span className="text-sm font-semibold text-gray-900">{item.value}</span>
                </div>
              ))}
            </div>
          </div>

          <div className="lg:col-span-2 bg-gradient-to-br from-white to-pink-50 rounded-xl shadow-lg border border-pink-100 p-6">
            <h2 className="text-xl font-bold bg-gradient-to-r from-pink-600 to-rose-600 bg-clip-text text-transparent mb-6">User Performance</h2>
            <div className="grid grid-cols-2 gap-6">
              <div className="bg-gradient-to-br from-blue-400 to-blue-600 rounded-lg p-6 shadow-md hover:shadow-lg transition-shadow duration-300">
                <Clock className="w-8 h-8 text-white mb-3" />
                <p className="text-sm text-blue-100 mb-1">Avg Tasks/User</p>
                <p className="text-3xl font-bold text-white">{userStats.avgTasksPerUser}</p>
                <p className="text-sm text-blue-100 mt-2">↑ 18% improvement</p>
              </div>
              <div className="bg-gradient-to-br from-teal-400 to-teal-600 rounded-lg p-6 shadow-md hover:shadow-lg transition-shadow duration-300">
                <Users className="w-8 h-8 text-white mb-3" />
                <p className="text-sm text-teal-100 mb-1">Top Performer</p>
                <p className="text-xl font-bold text-white">{userStats.topPerformer}</p>
                <p className="text-sm text-teal-100 mt-2">32 tasks completed</p>
              </div>
              <div className="bg-gradient-to-br from-purple-400 to-purple-600 rounded-lg p-6 shadow-md hover:shadow-lg transition-shadow duration-300">
                <TrendingUp className="w-8 h-8 text-white mb-3" />
                <p className="text-sm text-purple-100 mb-1">Team Velocity</p>
                <p className="text-3xl font-bold text-white">↑ 64%</p>
                <p className="text-sm text-purple-100 mt-2">Last 6 weeks</p>
              </div>
              <div className="bg-gradient-to-br from-pink-400 to-pink-600 rounded-lg p-6 shadow-md hover:shadow-lg transition-shadow duration-300">
                <CheckCircle className="w-8 h-8 text-white mb-3" />
                <p className="text-sm text-pink-100 mb-1">Completion Rate</p>
                <p className="text-3xl font-bold text-white">87%</p>
                <p className="text-sm text-pink-100 mt-2">↑ 12% this month</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}