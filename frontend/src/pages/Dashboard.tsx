import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { useAuth } from '../context/AuthContext';
import { dashboardService } from '../services/api';
import { Card, Badge, cn } from '../components/ui/core';
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  BarChart,
  Bar,
} from 'recharts';
import { Shield, FileCode, AlertTriangle, Zap, TrendingUp, Clock } from 'lucide-react';
import { motion } from 'framer-motion';

const StatCard = ({
  title,
  value,
  icon: Icon,
  color,
  delay,
  hint,
}: {
  title: string;
  value: string | number | undefined;
  icon: React.ElementType;
  color: string;
  delay: number;
  hint?: string;
}) => (
  <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay }}>
    <Card className="relative overflow-hidden group">
      <motion.div
        className={cn('absolute top-0 right-0 p-6 opacity-10 group-hover:scale-110 transition-transform duration-500', color)}
      >
        <Icon size={80} />
      </motion.div>
      <div className="relative z-10">
        <p className="text-muted-foreground text-sm font-medium mb-1">{title}</p>
        <div className="flex items-end gap-2">
          <h3 className="text-3xl font-bold">{value ?? 0}</h3>
          {hint && (
            <Badge variant="outline" className="mb-1">
              {hint}
            </Badge>
          )}
        </div>
      </div>
    </Card>
  </motion.div>
);

export const Dashboard = () => {
  const { user } = useAuth();

  const { data: summary, isLoading } = useQuery({
    queryKey: ['dashboardSummary', user?.id],
    queryFn: async () => (await dashboardService.getSummary()).data,
    enabled: !!user?.id,
  });

  if (isLoading) {
    return <motion.div className="p-8 text-center text-muted-foreground animate-pulse">Loading your analytics...</motion.div>;
  }

  return (
    <motion.div className="space-y-8">
      <motion.div>
        <h2 className="text-3xl font-bold tracking-tight mb-2">Welcome back, {user?.name}</h2>
        <p className="text-muted-foreground">Your personal code review and execution metrics.</p>
      </motion.div>

      <motion.div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatCard title="Your Projects" value={summary?.totalProjects} icon={Shield} color="text-primary" delay={0.1} />
        <StatCard title="Files Analyzed" value={summary?.totalFilesAnalyzed} icon={FileCode} color="text-purple-500" delay={0.2} />
        <StatCard
          title="Failed Executions"
          value={summary?.failedExecutions}
          icon={AlertTriangle}
          color="text-red-500"
          delay={0.3}
        />
        <StatCard
          title="Health Score"
          value={`${summary?.score ?? 0}%`}
          icon={Zap}
          color="text-yellow-500"
          delay={0.4}
          hint={`${summary?.successRate ?? 0}% run success`}
        />
      </motion.div>

      <motion.div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <Card className="p-0 overflow-hidden border-white/5">
          <div className="p-6 border-b border-white/5 flex items-center justify-between">
            <h4 className="font-semibold flex items-center gap-2">
              <TrendingUp className="w-4 h-4 text-primary" />
              Your Activity (7 days)
            </h4>
          </div>
          <div className="h-[300px] w-full p-6">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={summary?.activityData ?? []}>
                <defs>
                  <linearGradient id="colorIssues" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.3} />
                    <stop offset="95%" stopColor="#3b82f6" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#27272a" />
                <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fill: '#71717a', fontSize: 12 }} />
                <YAxis axisLine={false} tickLine={false} tick={{ fill: '#71717a', fontSize: 12 }} />
                <Tooltip
                  contentStyle={{ backgroundColor: '#09090b', borderColor: '#27272a', borderRadius: '8px' }}
                  itemStyle={{ color: '#fafafa' }}
                />
                <Area type="monotone" dataKey="issues" stroke="#3b82f6" fillOpacity={1} fill="url(#colorIssues)" />
                <Area type="monotone" dataKey="files" stroke="#8b5cf6" fill="transparent" strokeDasharray="5 5" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </Card>

        <Card className="p-0 overflow-hidden border-white/5">
          <div className="p-6 border-b border-white/5 flex items-center justify-between">
            <h4 className="font-semibold flex items-center gap-2">
              <Clock className="w-4 h-4 text-purple-500" />
              Analysis & Execution Mix
            </h4>
          </div>
          <div className="h-[300px] w-full p-6">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={summary?.issueDistribution ?? []}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#27272a" />
                <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fill: '#71717a', fontSize: 12 }} />
                <YAxis axisLine={false} tickLine={false} tick={{ fill: '#71717a', fontSize: 12 }} />
                <Tooltip
                  cursor={{ fill: 'rgba(255,255,255,0.05)' }}
                  contentStyle={{ backgroundColor: '#09090b', borderColor: '#27272a', borderRadius: '8px' }}
                />
                <Bar dataKey="count" fill="#8b5cf6" radius={[4, 4, 0, 0]} barSize={40} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </Card>
      </motion.div>

      {(summary?.recentActivity?.length ?? 0) > 0 && (
        <Card className="p-6 border-white/5">
          <h4 className="font-semibold mb-4">Recent Activity</h4>
          <ul className="space-y-2 text-sm text-muted-foreground">
            {summary?.recentActivity?.map((item, idx) => (
              <li key={idx} className="flex justify-between border-b border-white/5 pb-2">
                <span>{item.title}</span>
                <span>{new Date(item.at).toLocaleString()}</span>
              </li>
            ))}
          </ul>
        </Card>
      )}
    </motion.div>
  );
};
