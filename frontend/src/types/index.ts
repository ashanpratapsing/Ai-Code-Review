export type User = {
  id: string;
  email: string;
  name: string;
};

export type Project = {
  id: string;
  name: string;
  description: string;
  createdAt: string;
  updatedAt: string;
  language: string;
};

export type CodeFile = {
  id: string;
  name: string;
  content: string;
  projectId: string;
};

export type AnalysisResult = {
  fileId: string;
  status: string;
  score: number;
  summary: string;
  issues: string[];
  betterApproach?: string;
  timeComplexity?: string;
  spaceComplexity?: string;
  optimizedCode?: string;
  faangInsights?: string;
};

export type Issue = {
  line: number;
  severity: 'low' | 'medium' | 'high' | 'critical';
  message: string;
  suggestion: string;
};

export type DashboardSummary = {
  totalProjects: number;
  totalFilesAnalyzed: number;
  criticalIssues: number;
  score: number;
  activityData: { name: string; issues: number; files: number }[];
};
