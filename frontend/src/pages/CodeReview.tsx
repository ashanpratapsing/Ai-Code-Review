import React, { useState } from 'react';
import Editor from '@monaco-editor/react';
import { useMutation, useQuery } from '@tanstack/react-query';
import api from '../services/api';
import { Loader2, Play, Sparkles, Code2, Cpu, FileJson, Clock } from 'lucide-react';
import ReactMarkdown from 'react-markdown';
import { motion, AnimatePresence } from 'framer-motion';

export default function CodeReview() {
  const [code, setCode] = useState('// Select a file or paste code here...\n');
  const [selectedProjectId, setSelectedProjectId] = useState<number | null>(null);

  const { data: files, refetch: refetchFiles } = useQuery({
    queryKey: ['files', selectedProjectId],
    queryFn: async () => {
      const res = await api.get(selectedProjectId ? `/code/project/${selectedProjectId}` : '/code/files');
      return res.data;
    }
  });

  const analyzeMutation = useMutation({
    mutationFn: async (codeContent: string) => {
      // 1. Upload code to correct endpoint
      const fileRes = await api.post('/code/upload', {
        fileName: 'ReviewSnippet.java',
        codeContent,
        language: 'java',
        project: selectedProjectId ? { id: selectedProjectId } : null
      });
      // 2. Trigger AI review on the new file ID
      const [reviewRes, metricsRes] = await Promise.all([
        api.post(`/ai/review/${fileRes.data.id}`),
        api.post(`/analyze/${fileRes.data.id}`)
      ]);
      refetchFiles();
      return { review: reviewRes.data, metrics: metricsRes.data };
    }
  });

  const { data: projects } = useQuery({
    queryKey: ['projects'],
    queryFn: async () => {
      const res = await api.get('/projects');
      return res.data;
    }
  });

  return (
    <div className="flex h-screen bg-[#0d1117] text-white text-left overflow-hidden">
      
      {/* File Explorer Sidebar */}
      <div className="w-56 border-r border-[#30363d] bg-[#0d1117] flex flex-col">
         <div className="p-4 h-16 border-b border-[#30363d] flex items-center gap-2">
            <Clock className="w-4 h-4 text-gray-400" />
            <span className="text-[10px] font-black uppercase tracking-widest text-gray-500">History</span>
         </div>
         <div className="flex-1 overflow-y-auto p-2 space-y-1">
            {files?.map((f: any) => (
               <button 
                  key={f.id}
                  onClick={() => setCode(f.codeContent || '')}
                  className="w-full text-left p-3 rounded-lg hover:bg-[#161b22] text-xs font-medium text-gray-400 hover:text-blue-400 flex items-center gap-2 transition-all border border-transparent hover:border-[#30363d]"
               >
                  <FileJson className="w-4 h-4 opacity-50" />
                  <span className="truncate">{f.fileName}</span>
               </button>
            ))}
            {(!files || files.length === 0) && (
               <div className="p-4 text-[10px] text-gray-600 italic">No history yet</div>
            )}
         </div>
      </div>

      {/* Pane - Code Editor */}
      <div className="flex-1 border-r border-[#30363d] flex flex-col">
          <div className="p-4 bg-[#161b22] border-b border-[#30363d] flex justify-between items-center h-16">
              <div className="flex items-center gap-4">
                 <h2 className="text-sm font-black uppercase text-gray-500 tracking-widest flex items-center gap-2">
                    <Code2 className="w-4 h-4" /> IDE
                 </h2>
                 <select 
                    className="bg-[#0d1117] border border-[#30363d] rounded px-2 py-1 text-xs text-gray-300 outline-none focus:border-blue-500"
                    onChange={(e) => setSelectedProjectId(Number(e.target.value))}
                    value={selectedProjectId || ''}
                 >
                    <option value="">No Project</option>
                    {projects?.map((p: any) => (
                      <option key={p.id} value={p.id}>{p.name}</option>
                    ))}
                 </select>
              </div>
              <button 
                onClick={() => analyzeMutation.mutate(code)}
                disabled={analyzeMutation.isPending || !code.trim()}
                className="bg-blue-600 hover:bg-blue-500 disabled:bg-gray-800 disabled:text-gray-500 px-4 py-2 rounded-lg text-xs font-black uppercase tracking-tighter flex items-center gap-2 transition-all shadow-xl active:scale-95"
              >
                  {analyzeMutation.isPending ? <Loader2 className="animate-spin w-4 h-4" /> : <Play className="w-4 h-4" />}
                  Review with Groq
              </button>
          </div>
          <div className="flex-1 relative">
             <Editor
               height="100%"
               theme="vs-dark"
               defaultLanguage="java"
               value={code}
               onChange={(val) => setCode(val || '')}
               options={{ 
                 minimap: { enabled: false }, 
                 fontSize: 14, 
                 padding: { top: 20 },
                 fontFamily: "'JetBrains Mono', monospace",
                 smoothScrolling: true,
                 cursorBlinking: "expand"
               }}
             />
          </div>
      </div>

      {/* Right Pane - AI Analysis Output */}
      <div className="flex-1 flex flex-col bg-[#0d1117]">
          <div className="h-16 border-b border-[#30363d] p-4 flex items-center justify-between bg-[#0d1117]">
             <h2 className="text-sm font-black uppercase text-blue-500 tracking-widest flex items-center gap-2">
               <Cpu className="w-4 h-4" /> AI Diagnostics
             </h2>
             {analyzeMutation.isSuccess && (
                <div className="flex gap-4">
                   <div className="text-[10px] text-emerald-400 bg-emerald-500/10 border border-emerald-500/20 px-2 py-0.5 rounded uppercase font-bold">
                      Complexity: {analyzeMutation.data.metrics.complexityScore}
                   </div>
                   <div className="text-[10px] text-blue-400 bg-blue-500/10 border border-blue-500/20 px-2 py-0.5 rounded uppercase font-bold">
                      Functions: {analyzeMutation.data.metrics.numberOfFunctions}
                   </div>
                   <div className="text-[10px] text-purple-400 bg-purple-500/10 border border-purple-500/20 px-2 py-0.5 rounded uppercase font-bold">
                      Lines: {analyzeMutation.data.metrics.linesOfCode}
                   </div>
                </div>
             )}
          </div>
          
          <div className="flex-1 overflow-y-auto p-10 custom-scrollbar">
            <AnimatePresence mode="wait">
              {analyzeMutation.isPending ? (
                <motion.div 
                  initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
                  className="flex flex-col items-center justify-center h-full text-gray-400"
                >
                  <div className="relative mb-6">
                    <Loader2 className="animate-spin w-12 h-12 text-blue-600" />
                    <Sparkles className="absolute -top-1 -right-1 w-5 h-5 text-yellow-400 animate-pulse" />
                  </div>
                  <p className="font-bold uppercase tracking-tighter text-sm italic">AI Engine generating deep analysis...</p>
                  <p className="text-xs text-gray-500 mt-2">Checking complexity, potential bugs, and edge cases</p>
                </motion.div>
              ) : analyzeMutation.isSuccess ? (
                <motion.div 
                  initial={{ opacity: 0, x: 20 }} animate={{ opacity: 1, x: 0 }}
                  className="space-y-6"
                >
                  {/* Quantitative Stats Bar */}
                  <div className="grid grid-cols-3 gap-4 mb-6">
                     <div className="p-4 bg-[#161b22] border border-[#30363d] rounded-xl text-center">
                        <div className="text-[10px] text-gray-500 uppercase font-black mb-1">Complexity</div>
                        <div className="text-xl font-bold text-blue-400">{analyzeMutation.data.metrics.complexityScore}</div>
                     </div>
                     <div className="p-4 bg-[#161b22] border border-[#30363d] rounded-xl text-center">
                        <div className="text-[10px] text-gray-500 uppercase font-black mb-1">Loops</div>
                        <div className="text-xl font-bold text-yellow-400">{analyzeMutation.data.metrics.numberOfLoops}</div>
                     </div>
                     <div className="p-4 bg-[#161b22] border border-[#30363d] rounded-xl text-center">
                        <div className="text-[10px] text-gray-500 uppercase font-black mb-1">Functions</div>
                        <div className="text-xl font-bold text-purple-400">{analyzeMutation.data.metrics.numberOfFunctions}</div>
                     </div>
                  </div>

                  <div className="bg-[#161b22] border border-[#30363d] p-8 rounded-2xl shadow-2xl relative overflow-hidden">
                    <div className="absolute top-0 right-0 p-4 opacity-5">
                       <Sparkles className="w-24 h-24" />
                    </div>
                    <ReactMarkdown className="prose prose-invert prose-blue max-w-none">
                       {analyzeMutation.data.review.explanation}
                    </ReactMarkdown>
                  </div>
                </motion.div>
              ) : (
                <div className="h-full flex flex-col items-center justify-center border-2 border-dashed border-[#30363d] rounded-3xl opacity-30">
                  <Sparkles className="w-12 h-12 mb-4" />
                  <p className="text-sm font-bold uppercase tracking-widest">Input Code to Start Session</p>
                </div>
              )}
            </AnimatePresence>
          </div>
      </div>
    </div>
  );
}
