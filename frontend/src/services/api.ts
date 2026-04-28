import axios from 'axios';
import type { Project, CodeFile, AnalysisResult, DashboardSummary } from '../types';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8088',
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

export const historyService = {
  getHistory: () => api.get<any[]>('/history'),
  saveHistory: (data: any) => api.post('/history/save', data),
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
  analyzeBuffer: (content: string) => api.post<AnalysisResult>('/analyze/buffer', { content }),
};

export const visionService = {
  scanImage: (file: File) => {
    const formData = new FormData();
    formData.append('image', file);
    return api.post('/api/vision/scan', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
};

export const dashboardService = {
  getSummary: () => api.get<DashboardSummary>('/dashboard/summary'),
};

export default api;
