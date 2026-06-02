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
import { Shield, FileCode, AlertTriangle, Zap, TrendingUp, Clock, Calendar, Flame, Trophy, Activity, CheckCircle2 } from 'lucide-react';
import { motion } from 'framer-motion';
import { useTheme } from '../context/ThemeContext';
import { AxiomLogo } from '../components/AxiomLogo';

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
  const { theme } = useTheme();
  const isDark = theme === 'dark';

  const { data: summary, isLoading } = useQuery({
    queryKey: ['dashboardSummary', user?.id],
    queryFn: async () => (await dashboardService.getSummary()).data,
    enabled: !!user?.id,
  });

  const [hoveredDay, setHoveredDay] = React.useState<{
    date: Date;
    total: number;
    executions: number;
    reviews: number;
    rect: DOMRect;
  } | null>(null);

  const containerRef = React.useRef<HTMLDivElement>(null);

  const heatmapDays = React.useMemo(() => {
    const days = [];
    const today = new Date();
    
    const startDate = new Date();
    startDate.setDate(today.getDate() - 365);
    
    const startDayOfWeek = startDate.getDay(); 
    startDate.setDate(startDate.getDate() - startDayOfWeek);
    
    const currentDate = new Date(startDate);
    while (currentDate <= today) {
      days.push(new Date(currentDate));
      currentDate.setDate(currentDate.getDate() + 1);
    }
    return days;
  }, []);

  const monthLabels = React.useMemo(() => {
    const labels: { text: string; colIndex: number }[] = [];
    let lastMonth = -1;
    heatmapDays.forEach((day, index) => {
      if (day.getDay() === 0) {
        const month = day.getMonth();
        const colIndex = Math.floor(index / 7);
        if (month !== lastMonth) {
          labels.push({
            text: day.toLocaleDateString(undefined, { month: 'short' }),
            colIndex,
          });
          lastMonth = month;
        }
      }
    });
    return labels;
  }, [heatmapDays]);

  if (isLoading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] gap-4">
        <AxiomLogo size="md" showText={false} className="animate-pulse" />
        <span className="text-sm font-medium text-muted-foreground animate-pulse">Loading intelligence telemetry...</span>
      </div>
    );
  }

  const getCellColor = (count: number) => {
    if (isDark) {
      if (count === 0) return '#0B1020';
      if (count <= 2) return '#172554';
      if (count <= 4) return '#1D4ED8';
      if (count <= 6) return '#2563EB';
      return '#7C3AED';
    } else {
      if (count === 0) return '#F8FAFC';
      if (count <= 2) return '#DBEAFE';
      if (count <= 4) return '#93C5FD';
      if (count <= 6) return '#60A5FA';
      return '#7C3AED';
    }
  };

  const handleCellMouseEnter = (e: React.MouseEvent, day: Date, stats: { total: number; executions: number; reviews: number }) => {
    const rect = (e.target as HTMLElement).getBoundingClientRect();
    setHoveredDay({
      date: day,
      total: stats.total,
      executions: stats.executions,
      reviews: stats.reviews,
      rect,
    });
  };

  const handleCellMouseLeave = () => {
    setHoveredDay(null);
  };

  return (
    <motion.div className="space-y-8 relative" ref={containerRef}>
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

      {/* Streak and Contribution Heatmap Row */}
      <motion.div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <Card className="lg:col-span-2 p-0 overflow-hidden border-border-subtle flex flex-col justify-between">
          <div className="p-6 border-b border-border-subtle flex items-center justify-between">
            <h4 className="font-semibold flex items-center gap-2">
              <Calendar className="w-4 h-4 text-primary" />
              Developer Contribution Calendar
            </h4>
            <span className="text-xs text-muted-foreground font-mono">
              <span className="font-bold text-foreground">{summary?.totalContributions ?? 0}</span> Actions (1yr)
            </span>
          </div>

          <div className="p-6 overflow-x-auto">
            <div className="min-w-[760px] relative select-none">
              {/* Month labels row */}
              <div className="relative h-6 text-[10px] text-muted-foreground font-medium">
                {monthLabels.map((lbl, idx) => (
                  <span
                    key={idx}
                    className="absolute"
                    style={{ left: `${lbl.colIndex * 14 + 32}px` }}
                  >
                    {lbl.text}
                  </span>
                ))}
              </div>

              {/* Heatmap grid */}
              <div className="flex gap-2">
                {/* Day labels column */}
                <div className="flex flex-col justify-between text-[9px] text-muted-foreground w-6 pr-1 font-mono pt-[3px] h-[98px]">
                  <span>Mon</span>
                  <span>Wed</span>
                  <span>Fri</span>
                </div>

                {/* Cells grid */}
                <div className="grid grid-flow-col grid-rows-7 gap-[3px] h-[98px] grow">
                  {heatmapDays.map((day, idx) => {
                    const dateStr = day.toISOString().split('T')[0];
                    const stats = summary?.contributionCalendar?.[dateStr] || { total: 0, executions: 0, reviews: 0 };
                    return (
                      <div
                        key={idx}
                        className="w-[11px] h-[11px] rounded-[2px] cursor-pointer transition-colors duration-150 hover:ring-1 hover:ring-primary/50"
                        style={{ backgroundColor: getCellColor(stats.total) }}
                        onMouseEnter={(e) => handleCellMouseEnter(e, day, stats)}
                        onMouseLeave={handleCellMouseLeave}
                      />
                    );
                  })}
                </div>
              </div>
            </div>
          </div>

          <div className="px-6 pb-6 border-t border-border-subtle/50 pt-4 flex justify-between items-center text-xs text-muted-foreground">
            <span>Learn about consistency cycles</span>
            <div className="flex items-center gap-1.5">
              <span>Less</span>
              <div className="w-[10px] h-[10px] rounded-[2px]" style={{ backgroundColor: getCellColor(0) }} />
              <div className="w-[10px] h-[10px] rounded-[2px]" style={{ backgroundColor: getCellColor(1) }} />
              <div className="w-[10px] h-[10px] rounded-[2px]" style={{ backgroundColor: getCellColor(3) }} />
              <div className="w-[10px] h-[10px] rounded-[2px]" style={{ backgroundColor: getCellColor(5) }} />
              <div className="w-[10px] h-[10px] rounded-[2px]" style={{ backgroundColor: getCellColor(7) }} />
              <span>More</span>
            </div>
          </div>
        </Card>

        {/* Streak card */}
        <Card className="p-6 border-border-subtle flex flex-col justify-between overflow-hidden relative">
          <div className="absolute top-0 right-0 p-8 opacity-[0.02] pointer-events-none select-none text-purple-500 scale-150">
            <Flame size={180} />
          </div>
          <div>
            <div className="flex items-center justify-between mb-6">
              <h4 className="font-semibold flex items-center gap-2">
                <Flame className="w-4 h-4 text-purple-500 animate-pulse" />
                Developer Streak
              </h4>
              <Badge variant="outline" className="border-purple-500/20 text-purple-500 bg-purple-500/5 font-mono">
                Consistency Tracked
              </Badge>
            </div>

            <div className="flex items-center gap-4 mb-8">
              <div className="w-16 h-16 rounded-2xl bg-purple-500/5 border border-purple-500/10 flex items-center justify-center text-purple-500 shadow-inner">
                <Flame size={32} className="fill-purple-500/20" />
              </div>
              <div>
                <div className="text-4xl font-bold tracking-tight flex items-baseline gap-1.5 font-mono text-purple-500">
                  {summary?.currentStreak ?? 0}
                  <span className="text-xs text-muted-foreground font-sans font-normal">days active</span>
                </div>
                <div className="text-xs text-muted-foreground">Keep executing and analyzing to grow your streak!</div>
              </div>
            </div>

            {/* Streak Metrics Breakdown */}
            <div className="grid grid-cols-2 gap-4 border-t border-border-subtle pt-6">
              <div className="space-y-1">
                <span className="text-[10px] uppercase tracking-wider text-muted-foreground font-semibold flex items-center gap-1">
                  <Trophy className="w-3 h-3 text-yellow-500" />
                  Longest Streak
                </span>
                <p className="text-lg font-bold font-mono">{summary?.longestStreak ?? 0} Days</p>
              </div>
              <div className="space-y-1">
                <span className="text-[10px] uppercase tracking-wider text-muted-foreground font-semibold flex items-center gap-1">
                  <Activity className="w-3 h-3 text-primary" />
                  Consistency
                </span>
                <p className="text-lg font-bold font-mono">{summary?.weeklyConsistency ?? 0}%</p>
              </div>
              <div className="space-y-1">
                <span className="text-[10px] uppercase tracking-wider text-muted-foreground font-semibold flex items-center gap-1">
                  <CheckCircle2 className="w-3 h-3 text-green-500" />
                  Total Actions
                </span>
                <p className="text-lg font-bold font-mono">{summary?.totalContributions ?? 0}</p>
              </div>
              <div className="space-y-1">
                <span className="text-[10px] uppercase tracking-wider text-muted-foreground font-semibold flex items-center gap-1">
                  <Zap className="w-3 h-3 text-yellow-500" />
                  Success Rate
                </span>
                <p className="text-lg font-bold font-mono">{summary?.successRate ?? 0}%</p>
              </div>
            </div>
          </div>
        </Card>
      </motion.div>

      {/* Global absolute Tooltip */}
      {hoveredDay && containerRef.current && (
        <div
          className="absolute z-50 bg-popover text-popover-foreground border border-border-subtle rounded-xl p-3 shadow-2xl text-xs space-y-1.5 pointer-events-none transition-all duration-100 backdrop-blur-md"
          style={{
            left: hoveredDay.rect.left - containerRef.current.getBoundingClientRect().left + hoveredDay.rect.width / 2 - 80,
            top: hoveredDay.rect.top - containerRef.current.getBoundingClientRect().top - 95,
            width: '160px',
          }}
        >
          <div className="font-semibold text-foreground">
            {hoveredDay.date.toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' })}
          </div>
          <div className="text-muted-foreground">
            <span className="font-bold text-foreground">{hoveredDay.total}</span> contributions
          </div>
          {hoveredDay.total > 0 && (
            <div className="border-t border-border-subtle pt-1.5 mt-1 text-[10px] space-y-0.5 opacity-90">
              <div className="flex justify-between">
                <span>Executions</span>
                <span className="font-semibold font-mono">{hoveredDay.executions}</span>
              </div>
              <div className="flex justify-between">
                <span>AI Reviews</span>
                <span className="font-semibold font-mono">{hoveredDay.reviews}</span>
              </div>
            </div>
          )}
        </div>
      )}

      <motion.div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <Card className="p-0 overflow-hidden border-border-subtle">
          <div className="p-6 border-b border-border-subtle flex items-center justify-between">
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
                    <stop offset="5%" stopColor="var(--chart-stroke-1)" stopOpacity={0.3} />
                    <stop offset="95%" stopColor="var(--chart-stroke-1)" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="var(--chart-grid)" />
                <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fill: 'var(--chart-text)', fontSize: 12 }} />
                <YAxis axisLine={false} tickLine={false} tick={{ fill: 'var(--chart-text)', fontSize: 12 }} />
                <Tooltip
                  contentStyle={{ backgroundColor: 'var(--chart-tooltip-bg)', borderColor: 'var(--chart-tooltip-border)', borderRadius: '8px' }}
                  itemStyle={{ color: 'var(--chart-tooltip-text)' }}
                />
                <Area type="monotone" dataKey="issues" stroke="var(--chart-stroke-1)" fillOpacity={1} fill="url(#colorIssues)" />
                <Area type="monotone" dataKey="files" stroke="var(--chart-stroke-2)" fill="transparent" strokeDasharray="5 5" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </Card>

        <Card className="p-0 overflow-hidden border-border-subtle">
          <div className="p-6 border-b border-border-subtle flex items-center justify-between">
            <h4 className="font-semibold flex items-center gap-2">
              <Clock className="w-4 h-4 text-purple-500" />
              Analysis & Execution Mix
            </h4>
          </div>
          <div className="h-[300px] w-full p-6">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={summary?.issueDistribution ?? []}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="var(--chart-grid)" />
                <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fill: 'var(--chart-text)', fontSize: 12 }} />
                <YAxis axisLine={false} tickLine={false} tick={{ fill: 'var(--chart-text)', fontSize: 12 }} />
                <Tooltip
                  cursor={{ fill: 'var(--chart-cursor-fill)' }}
                  contentStyle={{ backgroundColor: 'var(--chart-tooltip-bg)', borderColor: 'var(--chart-tooltip-border)', borderRadius: '8px' }}
                />
                <Bar dataKey="count" fill="var(--chart-stroke-2)" radius={[4, 4, 0, 0]} barSize={40} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </Card>
      </motion.div>

      {(summary?.recentActivity?.length ?? 0) > 0 && (
        <Card className="p-6 border-border-subtle">
          <h4 className="font-semibold mb-4">Recent Activity</h4>
          <ul className="space-y-2 text-sm text-muted-foreground">
            {summary?.recentActivity?.map((item, idx) => (
              <li key={idx} className="flex justify-between border-b border-border-subtle pb-2">
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
