import React from 'react';
import { useQuery } from '@tanstack/react-query';
import api from '../services/api';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, LineChart, Line, CartesianGrid } from 'recharts';
import { Activity, Folder, FileType, CheckCircle2 } from 'lucide-react';
import { motion } from 'framer-motion';

export default function Dashboard() {
  const { data, isLoading } = useQuery({
    queryKey: ['dashboard-summary'],
    queryFn: async () => {
      const res = await api.get('/dashboard/summary');
      return res.data;
    }
  });

  // Mock timeline data since backend doesn't provide historical data yet
  const chartData = [
    { name: 'Mon', reports: 4, files: 10 },
    { name: 'Tue', reports: 7, files: 15 },
    { name: 'Wed', reports: 2, files: 8 },
    { name: 'Thu', reports: 12, files: 20 },
    { name: 'Fri', reports: 5, files: 12 },
    { name: 'Sat', reports: 1, files: 5 },
    { name: 'Sun', reports: 0, files: 2 },
  ];

  if (isLoading) return (
    <div className="p-8 flex items-center justify-center min-h-[400px]">
      <div className="animate-pulse text-blue-400">Loading metrics...</div>
    </div>
  );

  const stats = [
    { label: 'Total Projects', value: data?.totalProjects || 0, icon: Folder, color: 'text-blue-400' },
    { label: 'Files Uploaded', value: data?.totalFiles || 0, icon: FileType, color: 'text-purple-400' },
    { label: 'AI Reports', value: data?.totalAIReports || 0, icon: CheckCircle2, color: 'text-green-400' },
    { label: 'Metrics Generated', value: data?.totalMetrics || 0, icon: Activity, color: 'text-yellow-400' },
  ];

  return (
    <div className="p-8 bg-[#0d1117] min-h-screen text-white">
      <div className="max-w-7xl mx-auto">
        <div className="mb-10">
          <h1 className="text-3xl font-bold">Analytics Overview</h1>
          <p className="text-gray-400 mt-2">Real-time health of your code review projects.</p>
        </div>
        
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-10">
          {stats.map((stat, i) => (
            <motion.div 
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: i * 0.1 }}
              key={i} 
              className="glass-panel p-6 flex items-center justify-between border border-[#30363d] bg-[#161b22]"
            >
              <div>
                <p className="text-gray-400 text-xs uppercase tracking-wider font-bold mb-1">{stat.label}</p>
                <h3 className="text-3xl font-black">{stat.value}</h3>
              </div>
              <div className={`p-3 rounded-xl bg-gray-800/50 ${stat.color}`}>
                <stat.icon className="w-6 h-6" />
              </div>
            </motion.div>
          ))}
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          <motion.div 
             initial={{ opacity: 0, scale: 0.95 }}
             animate={{ opacity: 1, scale: 1 }}
             transition={{ delay: 0.4 }}
             className="glass-panel p-8 bg-[#161b22] border border-[#30363d] h-[450px]"
          >
            <h3 className="text-lg font-bold mb-8">Review Velocity</h3>
            <ResponsiveContainer width="100%" height="80%">
              <LineChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#30363d" vertical={false} />
                <XAxis dataKey="name" stroke="#8b949e" fontSize={12} tickLine={false} axisLine={false} />
                <YAxis stroke="#8b949e" fontSize={12} tickLine={false} axisLine={false} />
                <Tooltip 
                  contentStyle={{ backgroundColor: '#0d1117', border: '1px solid #30363d', borderRadius: '8px' }} 
                  itemStyle={{ color: '#2f81f7' }}
                />
                <Line type="monotone" dataKey="reports" stroke="#2f81f7" strokeWidth={4} dot={{ r: 4, fill: '#2f81f7' }} activeDot={{ r: 8 }} />
              </LineChart>
            </ResponsiveContainer>
          </motion.div>

          <motion.div 
             initial={{ opacity: 0, scale: 0.95 }}
             animate={{ opacity: 1, scale: 1 }}
             transition={{ delay: 0.5 }}
             className="glass-panel p-8 bg-[#161b22] border border-[#30363d] h-[450px]"
          >
            <h3 className="text-lg font-bold mb-8">Activity Distribution</h3>
            <ResponsiveContainer width="100%" height="80%">
              <BarChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#30363d" vertical={false} />
                <XAxis dataKey="name" stroke="#8b949e" fontSize={12} tickLine={false} axisLine={false} />
                <YAxis stroke="#8b949e" fontSize={12} tickLine={false} axisLine={false} />
                <Tooltip 
                  cursor={{ fill: '#21262d' }}
                  contentStyle={{ backgroundColor: '#0d1117', border: '1px solid #30363d', borderRadius: '8px' }}
                />
                <Bar dataKey="files" fill="#238636" radius={[4, 4, 0, 0]} barSize={20} />
              </BarChart>
            </ResponsiveContainer>
          </motion.div>
        </div>
      </div>
    </div>
  );
}
