import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useSearchParams } from 'react-router-dom';
import { motion } from 'framer-motion';
import { codeService } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { Card } from '../components/ui/core';
import { Code2, FileText } from 'lucide-react';
import { CodeAnalyzer } from '../components/CodeAnalyzer/CodeAnalyzer';
import type { CodeFile } from '../types';

export const CodeReview = () => {
  const { user } = useAuth();
  const [searchParams] = useSearchParams();
  const projectIdParam = searchParams.get('projectId');
  const projectId = projectIdParam ? Number(projectIdParam) : undefined;

  const [selectedFile, setSelectedFile] = useState<CodeFile | null>(null);

  const { data: files } = useQuery({
    queryKey: ['files', user?.id, projectId],
    queryFn: () => codeService.getFiles(projectId).then((r) => r.data),
    enabled: !!user?.id && !!projectId,
  });

  if (!projectId) {
    return (
      <motion.p className="p-12 text-center text-muted-foreground" initial={{ opacity: 0 }} animate={{ opacity: 1 }}>
        Select a project from the Projects page to start reviewing code.
      </motion.p>
    );
  }

  return (
    <motion.div className="h-[calc(100vh-10rem)] flex gap-6 p-2" initial={{ opacity: 0 }} animate={{ opacity: 1 }}>
      <Card className="w-72 p-0 flex flex-col border-white/5 bg-black/40 backdrop-blur-3xl overflow-hidden shadow-2xl">
        <div className="p-5 border-b border-white/5">
          <h5 className="font-bold text-xs uppercase tracking-widest text-muted-foreground flex items-center gap-2">
            <Code2 className="w-4 h-4 text-primary" />
            Project Files
          </h5>
        </motion.div>

        <div className="flex-1 overflow-y-auto p-3 space-y-1">
          {files?.map((file) => (
            <button
              key={file.id}
              type="button"
              onClick={() => setSelectedFile(file)}
              className={`w-full text-left px-4 py-3 rounded-xl text-sm transition-all flex items-center gap-3 border ${
                selectedFile?.id === file.id
                  ? 'bg-primary/10 border-primary/30 text-primary'
                  : 'border-transparent hover:bg-white/5 text-muted-foreground'
              }`}
            >
              <FileText className="w-4 h-4 opacity-60" />
              <span className="truncate">{file.name}</span>
            </button>
          ))}
          {!files?.length && (
            <p className="py-20 text-center opacity-40 text-xs italic">No files yet — analyze to upload</p>
          )}
        </motion.div>
      </Card>

      <motion.div className="flex-1 min-w-0">
        <CodeAnalyzer
          projectId={projectId}
          fileId={selectedFile?.id}
          initialCode={selectedFile?.content}
          key={selectedFile?.id ?? 'new'}
        />
      </motion.div>
    </motion.div>
  );
};
