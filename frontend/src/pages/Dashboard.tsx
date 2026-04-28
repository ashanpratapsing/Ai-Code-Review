import { useQuery } from '@tanstack/react-query';
import { dashboardService } from '../services/api';
import { Card, Badge, cn } from '../components/ui/core';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, BarChart, Bar } from 'recharts';
import { Shield, FileCode, AlertTriangle, TrendingUp, Zap, Clock } from 'lucide-react';
import { motion } from 'framer-motion';

const StatCard = ({ title, value, icon: Icon, color, delay }: any) => (
  <motion.div
    initial={{ opacity: 0, y: 20 }}
    animate={{ opacity: 1, y: 0 }}
    transition={{ delay }}
  >
    <Card className="relative overflow-hidden group">
      <div className={cn("absolute top-0 right-0 p-6 opacity-10 group-hover:scale-110 transition-transform duration-500", color)}>
        <Icon size={80} />
      </div>
      <div className="relative z-10">
        <p className="text-muted-foreground text-sm font-medium mb-1">{title}</p>
        <div className="flex items-end gap-2">
          <h3 className="text-3xl font-bold">{value}</h3>
          <Badge variant="success" className="mb-1">+12%</Badge>
        </div>
      </div>
    </Card>
  </motion.div>
);

export const Dashboard = () => {
  const { data: summary, isLoading } = useQuery({
    queryKey: ['dashboardSummary'],
    queryFn: async () => {
      // Mock data if API fails or for demo
      return {
        totalProjects: 12,
        totalFilesAnalyzed: 154,
        criticalIssues: 8,
        score: 92,
        activityData: [
          { name: 'Mon', issues: 4, files: 12 },
          { name: 'Tue', issues: 7, files: 15 },
          { name: 'Wed', issues: 3, files: 10 },
          { name: 'Thu', issues: 12, files: 25 },
          { name: 'Fri', issues: 5, files: 18 },
          { name: 'Sat', issues: 2, files: 8 },
          { name: 'Sun', issues: 1, files: 5 },
        ]
      };
    }
  });

  if (isLoading) return <div className="p-8 text-center text-muted-foreground animate-pulse">Loading analytics...</div>;

  return (
    <div className="space-y-8">
      <div>
        <h2 className="text-3xl font-bold tracking-tight mb-2">System Overview</h2>
        <p className="text-muted-foreground">Monitor your project health and AI analysis metrics.</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatCard title="Active Projects" value={summary?.totalProjects} icon={Shield} color="text-primary" delay={0.1} />
        <StatCard title="Files Analyzed" value={summary?.totalFilesAnalyzed} icon={FileCode} color="text-purple-500" delay={0.2} />
        <StatCard title="Critical Issues" value={summary?.criticalIssues} icon={AlertTriangle} color="text-red-500" delay={0.3} />
        <StatCard title="Health Score" value={summary?.score + '%'} icon={Zap} color="text-yellow-500" delay={0.4} />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <Card className="p-0 overflow-hidden border-white/5">
          <div className="p-6 border-b border-white/5 flex items-center justify-between">
            <h4 className="font-semibold flex items-center gap-2">
              <TrendingUp className="w-4 h-4 text-primary" />
              Analysis Activity
            </h4>
            <div className="flex gap-2">
              <Badge variant="outline">7 Days</Badge>
            </div>
          </div>
          <div className="h-[300px] w-full p-6">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={summary?.activityData}>
                <defs>
                  <linearGradient id="colorIssues" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.3}/>
                    <stop offset="95%" stopColor="#3b82f6" stopOpacity={0}/>
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#27272a" />
                <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{fill: '#71717a', fontSize: 12}} />
                <YAxis axisLine={false} tickLine={false} tick={{fill: '#71717a', fontSize: 12}} />
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
              Issue Distribution
            </h4>
          </div>
          <div className="h-[300px] w-full p-6 text-center flex items-center justify-center">
             {/* Mock Bar Chart */}
             <ResponsiveContainer width="100%" height="100%">
                <BarChart data={[
                  { name: 'Security', count: 12 },
                  { name: 'Perf', count: 8 },
                  { name: 'Style', count: 24 },
                  { name: 'Logic', count: 5 },
                ]}>
                   <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#27272a" />
                   <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{fill: '#71717a', fontSize: 12}} />
                   <YAxis axisLine={false} tickLine={false} tick={{fill: '#71717a', fontSize: 12}} />
                   <Tooltip 
                     cursor={{fill: 'rgba(255,255,255,0.05)'}}
                     contentStyle={{ backgroundColor: '#09090b', borderColor: '#27272a', borderRadius: '8px' }}
                   />
                   <Bar dataKey="count" fill="#8b5cf6" radius={[4, 4, 0, 0]} barSize={40} />
                </BarChart>
             </ResponsiveContainer>
          </div>
        </Card>
      </div>
    </div>
  );
};
