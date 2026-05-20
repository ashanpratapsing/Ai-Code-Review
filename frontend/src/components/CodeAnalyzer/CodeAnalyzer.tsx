import React from 'react';
import { motion } from 'framer-motion';
import { useCodeAnalysis } from './hooks/useCodeAnalysis';
import { CodeInput } from './CodeInput';
import { AnalysisResults } from './AnalysisResults';
import { LoadingState } from './LoadingState';
import { AlertCircle, RotateCcw } from 'lucide-react';
import { Button } from '../ui/core';
import type { AnalysisContext } from '../../types';

interface CodeAnalyzerProps {
  projectId?: number;
  initialCode?: string;
  fileId?: number;
}

export const CodeAnalyzer: React.FC<CodeAnalyzerProps> = ({ projectId, initialCode, fileId }) => {
  const { analyze, loading, status, error, results, setResults } = useCodeAnalysis(projectId, fileId);

  if (loading) {
    return <LoadingState status={status} />;
  }

  if (error) {
    return (
      <motion.div className="h-full flex flex-col items-center justify-center p-12 text-center space-y-6">
        <motion.div className="w-20 h-20 bg-red-500/10 rounded-full flex items-center justify-center border border-red-500/20">
          <AlertCircle className="w-10 h-10 text-red-500" />
        </motion.div>
        <div className="space-y-2">
          <h3 className="text-xl font-bold tracking-tight">Analysis Interrupted</h3>
          <p className="text-sm text-destructive font-medium bg-red-500/10 px-4 py-2 rounded-lg border border-red-500/20">
            {error}
          </p>
        </motion.div>
        <Button onClick={() => setResults(null)} variant="outline" className="gap-2 px-8 rounded-full">
          <RotateCcw className="w-4 h-4" />
          Try Again
        </Button>
      </motion.div>
    );
  }

  if (results) {
    return <AnalysisResults results={results} onReset={() => setResults(null)} />;
  }

  return (
    <CodeInput
      onAnalyze={(code, language, model, executionContext?: AnalysisContext) =>
        analyze(code, language, model, executionContext)
      }
      isLoading={loading}
      initialCode={initialCode}
      codeFileId={fileId}
    />
  );
};
