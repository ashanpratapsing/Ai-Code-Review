import axios from 'axios';
import type {
  Project,
  CodeFile,
  AnalysisResult,
  DashboardSummary,
  ExecutionResponse,
  AnalysisContext,
  HistoryItem,
} from '../types';

const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export const api = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true,
});

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    if (error.response?.status === 401 && !originalRequest?._retry) {
      originalRequest._retry = true;
      try {
        await api.post('/auth/refresh');
        return api(originalRequest);
      } catch {
        if (typeof window !== 'undefined' && !window.location.pathname.includes('/login')) {
          window.location.href = '/login';
        }
        return Promise.reject(error);
      }
    }
    return Promise.reject(error);
  }
);

export const authService = {
  login: (credentials: { email: string; password: string }) => api.post('/auth/login', credentials),
  register: (data: { name: string; email: string; password: string }) => api.post('/auth/signup', data),
  logout: () => api.post('/auth/logout'),
  me: () => api.get('/auth/me'),
};

export const historyService = {
  getHistory: () => api.get<HistoryItem[]>('/history'),
  saveHistory: (data: { codeSnippet: string; resultJson: string; score?: number; codeFileId?: number }) =>
    api.post('/history/save', data),
};

export const projectService = {
  getProjects: () => api.get<Project[]>('/projects'),
  createProject: (data: Partial<Project>) => api.post<Project>('/projects', data),
  deleteProject: (id: number) => api.delete(`/projects/${id}`),
  updateProject: (id: number, data: Partial<Project>) => api.put<Project>(`/projects/${id}`, data),
};

export const codeService = {
  uploadCode: (data: { name: string; content: string; language: string; project: { id: number } }) =>
    api.post<CodeFile>('/code/upload', data),
  getFiles: (projectId?: number) => {
    const url = projectId ? `/code/files?projectId=${projectId}` : '/code/files';
    return api.get<CodeFile[]>(url);
  },
  getFile: (fileId: number) => api.get<CodeFile>(`/code/files/${fileId}`),
};

export const aiService = {
  analyzeFile: (fileId: number, model: string = 'AUTO', executionContext?: AnalysisContext) =>
    api.post(`/analyze/${fileId}?model=${model}`, executionContext ?? {}),
  getMetrics: (fileId: number) => api.get<AnalysisResult>(`/analyze/${fileId}`),
};

export const dashboardService = {
  getSummary: () => api.get<DashboardSummary>('/dashboard/summary'),
};

export const codeExecutionService = {
  execute: (data: {
    code: string;
    language: string;
    testCases: Array<{ id: number; input: string; expectedOutput: string }>;
    codeFileId?: number;
  }) => api.post<ExecutionResponse>('/execute', data),
};

export const oauthUrl = (provider: 'github' | 'google') =>
  `${API_BASE}/oauth2/authorization/${provider}`;

export default api;
