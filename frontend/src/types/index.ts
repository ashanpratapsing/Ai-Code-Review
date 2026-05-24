export interface User {
  id: number;
  email: string;
  name: string;
  role?: string;
}

export interface Project {
  id: number;
  projectName?: string;
  name?: string;
  description?: string;
  createdAt?: string;
  language?: string;
}

export interface CodeFile {
  id: number;
  name: string;
  content: string;
  language?: string;
  projectId?: number;
}

export interface TestCase {
  id: number;
  input: string;
  expectedOutput: string;
}

export interface TestCaseResult {
  id: number;
  status: 'PASSED' | 'FAILED' | 'RUNTIME_ERROR' | 'TIMEOUT';
  actualOutput?: string;
  expectedOutput?: string;
  error?: string;
  executionTimeMs?: number;
}

export interface ExecutionResponse {
  executionId?: number;
  status: string;
  compileError?: string;
  results?: TestCaseResult[];
}

export interface AnalysisContext {
  executionStatus?: string;
  compileError?: string;
  executionResults?: TestCaseResult[];
}

export interface AnalysisResult {
  status?: string;
  score?: number;
  summary?: string | { score?: number; text?: string; quality?: string };
  codeQuality?: string;
  explanation?: string;
  bugsDetected?: string[];
  issues?: string[];
  securityIssues?: string[];
  suggestions?: string[];
  betterApproach?: string;
  optimizedCode?: string;
  designPattern?: string;
  faangInsights?: string;
  edgeCases?: string[];
  performanceIssues?: string[];
  bestPractices?: string[];
  codeSmells?: string[];
  scalabilityAnalysis?: string;
  readabilityScore?: number;
  maintainabilityScore?: number;
  timeComplexity?: string;
  spaceComplexity?: string;
  rootCause?: string;
  concurrencyAnalysis?: string;
  collectionAnalysis?: string;
  graphAnalysis?: string;
  runtimeAnalysis?: string;
}

export interface DashboardSummary {
  totalProjects: number;
  totalFilesAnalyzed: number;
  totalAnalyses: number;
  failedExecutions: number;
  passedExecutions: number;
  successRate: number;
  score: number;
  activityData: { name: string; issues: number; files: number }[];
  issueDistribution: { name: string; count: number }[];
  recentActivity?: { type: string; title: string; status?: string; score?: number; at: string }[];
}

export interface HistoryItem {
  id: number;
  codeSnippet: string;
  resultJson: string;
  score?: number;
  createdAt: string | number[];
  projectId?: number;
  codeFileId?: number;
}
