import React from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { codeService } from '../services/api';
import { Card } from '../components/ui/core';
import { 
  Plus, 
  Code2,
  FileText
} from 'lucide-react';
import { CodeAnalyzer } from '../components/CodeAnalyzer/CodeAnalyzer';

export const CodeReview = () => {
  const queryParams = new URLSearchParams(window.location.search);
  const projectId = queryParams.get('projectId') || 'all';

  const { data: files } = useQuery({
    queryKey: ['files', projectId],
    queryFn: () => codeService.getFiles(projectId).then(r => r.data)
  });

  return (
    <div className="h-[calc(100vh-10rem)] flex gap-6 p-2">
      {/* 1. Project Navigation Sidebar */}
      <Card className="w-72 p-0 flex flex-col border-white/5 bg-black/40 backdrop-blur-3xl overflow-hidden shadow-2xl">
        <div className="p-5 border-b border-white/5 bg-white/5 flex justify-between items-center bg-gradient-to-r from-primary/10 to-transparent">
          <h5 className="font-bold text-xs uppercase tracking-widest text-muted-foreground flex items-center gap-2">
            <Code2 className="w-4 h-4 text-primary" />
            Project Hub
          </h5>
          <button 
            className="h-8 w-8 rounded-lg hover:bg-primary/20 text-primary transition-all active:scale-95 flex items-center justify-center border border-white/5"
            onClick={() => {
              const name = prompt('File Name (e.g. Solution.java):');
              if (name) {
                // Future functionality: create empty file or select context
                console.log('Creating file:', name);
              }
            }}
          >
            <Plus className="w-4 h-4" />
          </button>
        </div>
        
        <div className="flex-1 overflow-y-auto p-3 space-y-1">
          {files?.map(file => (
            <div
              key={file.id}
              className="w-full text-left px-4 py-3 rounded-xl text-sm transition-all flex items-center gap-3 border border-transparent hover:bg-white/5 text-muted-foreground cursor-pointer"
            >
              <FileText className="w-4 h-4 opacity-40" />
              <span className="truncate">{file.name}</span>
            </div>
          ))}
          {!files?.length && (
             <div className="py-20 text-center opacity-20">
                <FileText className="w-12 h-12 mx-auto mb-2" />
                <p className="text-xs italic">Workspace Empty</p>
             </div>
          )}
        </div>
      </Card>

      {/* 2. Main Collaborative Analyzer */}
      <div className="flex-1 min-w-0">
        <CodeAnalyzer />
      </div>
    </div>
  );
};
