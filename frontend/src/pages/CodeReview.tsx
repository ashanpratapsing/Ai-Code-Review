import React, { useState, useEffect } from 'react';
import Editor from '@monaco-editor/react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { codeService, aiService } from '../services/api';
import { Button, Card, Badge, cn } from '../components/ui/core';
import { Play, Sparkles, FileText, ChevronRight, AlertCircle, Info, CheckCircle2, Plus } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import ReactMarkdown from 'react-markdown';

export const CodeReview = () => {
  const queryClient = useQueryClient();
  const [code, setCode] = useState('// Select a file to start reviewing');
  const [selectedFileId, setSelectedFileId] = useState<string | null>(null);
  const [reviewPanelOpen, setReviewPanelOpen] = useState(true);

  const { data: files } = useQuery({
    queryKey: ['files'],
    queryFn: () => codeService.getFiles('default-project').then(r => r.data)
  });

  const analyzeMutation = useMutation({
    mutationFn: (id: string) => aiService.analyzeFile(id),
  });

  const aiReviewMutation = useMutation({
    mutationFn: (id: string) => aiService.getAiReview(id),
  });

  const uploadMutation = useMutation({
    mutationFn: (data: any) => codeService.uploadCode(data),
    onSuccess: () => {
       queryClient.invalidateQueries({ queryKey: ['files'] });
    }
  });

  const handleFileSelect = (file: any) => {
    setSelectedFileId(file.id);
    setCode(file.codeContent);
  };

  const handleStartReview = async () => {
    if (selectedFileId) {
       analyzeMutation.mutate(selectedFileId);
       aiReviewMutation.mutate(selectedFileId);
    } else {
       if (!code.trim() || code.includes('// Select a file')) return;
       try {
         const response = await codeService.uploadCode({
           fileName: 'Untitled.ts',
           codeContent: code,
           language: 'typescript'
         });
         const newFileId = response.data.id;
         setSelectedFileId(newFileId);
         queryClient.invalidateQueries({ queryKey: ['files'] });
         
         analyzeMutation.mutate(newFileId);
         aiReviewMutation.mutate(newFileId);
       } catch (err) {
         console.error("Auto-upload failed", err);
       }
    }
  };

  return (
    <div className="h-[calc(100vh-12rem)] flex gap-6">
      {/* File Explorer */}
      <Card className="w-64 p-0 flex flex-col border-white/5 overflow-hidden">
        <div className="p-4 border-b border-white/5 bg-white/5 flex justify-between items-center">
          <h5 className="font-semibold text-sm flex items-center gap-2">
            <FileText className="w-4 h-4" />
            Explorer
          </h5>
          <Button 
            variant="ghost" 
            size="icon" 
            className="h-6 w-6 rounded-md hover:bg-primary/20 text-primary"
            onClick={() => {
              const name = prompt('File Name (e.g. Solution.java):');
              if (name) {
                uploadMutation.mutate({
                  fileName: name,
                  codeContent: code,
                  language: name.split('.').pop() || 'txt'
                });
              }
            }}
          >
            <Plus className="w-3 h-3" />
          </Button>
        </div>
        <div className="flex-1 overflow-y-auto p-2">
          {files?.map(file => (
            <button
              key={file.id}
              onClick={() => handleFileSelect(file)}
              className={cn(
                "w-full text-left px-3 py-2 rounded-md text-sm transition-colors flex items-center gap-2",
                selectedFileId === file.id ? "bg-primary/10 text-primary" : "hover:bg-white/5 text-muted-foreground"
              )}
            >
              <FileText className="w-4 h-4 opacity-50" />
              {file.name}
            </button>
          ))}
          {!files && <p className="text-xs text-muted-foreground p-4 italic text-center">No files found.</p>}
        </div>
      </Card>

      {/* Main Editor Area */}
      <div className="flex-1 flex flex-col gap-4 min-w-0">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <h3 className="text-xl font-bold tracking-tight">
              {files?.find(f => f.id === selectedFileId)?.name || 'Editor'}
            </h3>
            <Badge variant="outline">TypeScript</Badge>
          </div>
          <div className="flex gap-2">
            <Button variant="outline" size="sm" onClick={() => setReviewPanelOpen(!reviewPanelOpen)}>
              {reviewPanelOpen ? 'Hide Review' : 'Show Review'}
            </Button>
            <Button size="sm" className="gap-2" onClick={handleStartReview} disabled={!code.trim() || code.includes('// Select a file') || analyzeMutation.isPending}>
              <Sparkles className="w-4 h-4" />
              Analyze Code
            </Button>
          </div>
        </div>

        <Card className="flex-1 p-0 overflow-hidden border-white/5 bg-[#1e1e1e]">
          <Editor
            height="100%"
            defaultLanguage="typescript"
            theme="vs-dark"
            value={code}
            onChange={(val) => setCode(val || '')}
            options={{
              fontSize: 14,
              minimap: { enabled: false },
              padding: { top: 20 },
              smoothScrolling: true,
              cursorBlinking: 'smooth',
            }}
          />
        </Card>
      </div>

      {/* AI Review Panel */}
      <AnimatePresence>
        {reviewPanelOpen && (
          <motion.div
            initial={{ width: 0, opacity: 0 }}
            animate={{ width: 400, opacity: 1 }}
            exit={{ width: 0, opacity: 0 }}
            className="flex flex-col gap-4"
          >
            <Card className="flex-1 p-0 flex flex-col border-white/5 overflow-hidden">
              <div className="p-4 border-b border-white/5 bg-primary/5 flex items-center justify-between">
                <h5 className="font-semibold text-sm flex items-center gap-2">
                  <Sparkles className="w-4 h-4 text-primary" />
                  AI Analysis
                </h5>
                {analyzeMutation.isSuccess && <Badge variant="success">Complete</Badge>}
              </div>

              <div className="flex-1 overflow-y-auto p-4 space-y-4">
                {analyzeMutation.isPending && (
                  <div className="space-y-4">
                    {[1, 2, 3].map(i => (
                      <div key={i} className="h-24 bg-white/5 rounded-lg animate-pulse" />
                    ))}
                  </div>
                )}

                {analyzeMutation.data && (
                  <div className="space-y-4">
                    <div className="p-4 rounded-lg bg-red-500/10 border border-red-500/20">
                       <p className="text-sm font-medium flex items-center gap-2 text-red-500 mb-2">
                          <AlertCircle className="w-4 h-4" />
                          Critical Issue Found
                       </p>
                       <p className="text-xs text-muted-foreground">{analyzeMutation.data.data.summary}</p>
                    </div>
                    
                    {analyzeMutation.data.data.issues.map((issue, idx) => (
                      <div key={idx} className="p-4 rounded-lg bg-secondary/50 border border-border">
                        <div className="flex justify-between mb-2">
                          <span className="text-xs font-mono text-muted-foreground">Line {issue.line}</span>
                          <Badge variant={issue.severity === 'high' ? 'error' : 'warning'}>{issue.severity}</Badge>
                        </div>
                        <p className="text-sm mb-2">{issue.message}</p>
                        <div className="p-2 rounded bg-background/50 text-xs font-mono border border-border">
                          {issue.suggestion}
                        </div>
                      </div>
                    ))}
                  </div>
                )}

                {aiReviewMutation.data && (
                  <div className="prose prose-invert prose-sm pb-8">
                    <ReactMarkdown>
                      {`
${aiReviewMutation.data.data.explanation ? `### 📝 Summary\n${aiReviewMutation.data.data.explanation}\n\n` : ''}
${aiReviewMutation.data.data.bugs ? `### 🐛 Bugs & Issues\n${aiReviewMutation.data.data.bugs}\n\n` : ''}
${aiReviewMutation.data.data.optimization ? `### ⚡ Optimization\n${aiReviewMutation.data.data.optimization}\n\n` : ''}
${aiReviewMutation.data.data.codeSmells ? `### 👃 Code Smells\n${aiReviewMutation.data.data.codeSmells}\n\n` : ''}
${aiReviewMutation.data.data.timeComplexity ? `### ⏱️ Complexity\n**Time & Space:** ${aiReviewMutation.data.data.timeComplexity}\n\n` : ''}
${aiReviewMutation.data.data.refactoredCode ? `### ✨ Recommended Refactor\n\`\`\`typescript\n${aiReviewMutation.data.data.refactoredCode}\n\`\`\`\n\n` : ''}
${aiReviewMutation.data.data.unitTests ? `### 🧪 Unit Tests\n\`\`\`typescript\n${aiReviewMutation.data.data.unitTests}\n\`\`\`\n\n` : ''}
                      `}
                    </ReactMarkdown>
                  </div>
                )}

                {!analyzeMutation.isIdle || (
                   <div className="text-center py-12">
                      <Sparkles className="w-12 h-12 text-muted-foreground mx-auto mb-4 opacity-20" />
                      <p className="text-sm text-muted-foreground px-8">
                        Upload or select a file and click "Analyze" to see AI-powered suggestions.
                      </p>
                   </div>
                )}
              </div>
            </Card>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};
