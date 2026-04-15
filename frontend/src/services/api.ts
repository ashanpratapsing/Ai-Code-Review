import axios from 'axios';
import type { Project, CodeFile, AnalysisResult, DashboardSummary } from '../types';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const authService = {
  login: (credentials: any) => api.post('/auth/login', credentials),
  register: (data: any) => api.post('/auth/signup', data),
};

export const projectService = {
  getProjects: () => api.get<Project[]>('/projects'),
  createProject: (data: Partial<Project>) => api.post<Project>('/projects', data),
  deleteProject: (id: string) => api.delete(`/projects/${id}`),
};

export const codeService = {
  uploadCode: (data: any) => api.post<CodeFile>('/code/upload', data),
  getFiles: (projectId: string) => api.get<CodeFile[]>(`/code/files?projectId=${projectId}`),
  getFile: (fileId: string) => api.get<CodeFile>(`/code/files/${fileId}`),
};

export const aiService = {
  analyzeFile: (fileId: string) => api.post<AnalysisResult>(`/analyze/${fileId}`),
  getAiReview: (fileId: string) => api.post<any>(`/ai/review/${fileId}`),
};

export const dashboardService = {
  getSummary: () => api.get<DashboardSummary>('/dashboard/summary'),
};

export default api;
