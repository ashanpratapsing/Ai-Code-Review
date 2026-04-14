import React, { useState } from 'react';
import Editor from '@monaco-editor/react';
import { useMutation } from '@tanstack/react-query';
import api from '../services/api';
import { Loader2, Play, LogOut } from 'lucide-react';
import ReactMarkdown from 'react-markdown';
import { useAuth } from '../context/AuthContext';

export default function CodeReview() {
  const [code, setCode] = useState('// Write your code here\n');
  const { logout } = useAuth();

  const analyzeMutation = useMutation({
    mutationFn: async (codeContent: string) => {
      // Create file in backend
      const fileRes = await api.post('/files', {
        fileName: 'App.java',
        codeContent,
        language: 'java'
      });
      // Run AI review
      const reviewRes = await api.post(`/ai/review/${fileRes.data.id}`);
      return reviewRes.data;
    }
  });

  return (
    <div className="flex h-screen bg-[#0d1117] text-white">
      {/* Sidebar/Left Code Editor */}
      <div className="w-1/2 border-r border-gray-800 flex flex-col relative">
          <div className="p-4 border-b border-gray-800 flex justify-between items-center bg-[#161b22]">
              <h2 className="text-sm font-semibold flex items-center gap-4">
                 Editor
                 <button onClick={logout} className="text-gray-400 hover:text-white flex items-center gap-1 text-xs">
                    <LogOut className="w-3 h-3" /> Logout
                 </button>
              </h2>
              <button 
                onClick={() => analyzeMutation.mutate(code)}
                disabled={analyzeMutation.isPending}
                className="bg-blue-600 hover:bg-blue-500 disabled:opacity-50 px-4 py-2 rounded-md text-sm font-medium flex items-center gap-2 transition-all">
                  {analyzeMutation.isPending ? <Loader2 className="animate-spin w-4 h-4" /> : <Play className="w-4 h-4" />}
                  Analyze with AI
              </button>
          </div>
          <Editor
            height="100%"
            theme="vs-dark"
            defaultLanguage="java"
            value={code}
            onChange={(val) => setCode(val || '')}
            options={{ minimap: { enabled: false }, fontSize: 14 }}
          />
      </div>

      {/* Right AI Analysis Output */}
      <div className="w-1/2 p-8 overflow-y-auto bg-[#0d1117]">
          <h2 className="text-2xl font-bold mb-6 flex items-center gap-2 text-blue-400">
            ✨ AI Analysis Results
          </h2>
          
          {analyzeMutation.isPending && (
             <div className="flex flex-col items-center justify-center h-64 text-gray-400">
                <Loader2 className="animate-spin w-8 h-8 mb-4 text-blue-500" />
                <p>Generating deep actionable insights...</p>
             </div>
          )}

          {analyzeMutation.isSuccess && (
             <div className="prose prose-invert max-w-none glass-panel p-6 border border-gray-800 rounded-xl bg-[#161b22]">
                <ReactMarkdown>{analyzeMutation.data.explanation}</ReactMarkdown>
             </div>
          )}

          {analyzeMutation.isError && (
             <div className="text-red-400 flex items-center justify-center p-4 border border-red-900 bg-red-900/20 rounded-md">
                Error during analysis. Please check console or try again.
             </div>
          )}

          {!analyzeMutation.isPending && !analyzeMutation.isSuccess && !analyzeMutation.isError && (
             <div className="text-gray-500 flex items-center justify-center h-64 border border-dashed border-gray-800 rounded-xl">
                Ready. Run an analysis to see insights here.
             </div>
          )}
      </div>
    </div>
  );
}
