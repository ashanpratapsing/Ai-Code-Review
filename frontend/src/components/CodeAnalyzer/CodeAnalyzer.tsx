import React from 'react';
import { useCodeAnalysis } from './hooks/useCodeAnalysis';
import { CodeInput } from './CodeInput';
import { AnalysisResults } from './AnalysisResults';
import { LoadingState } from './LoadingState';
import { AlertCircle, RotateCcw } from 'lucide-react';
import { Button } from '../ui/core';

export const CodeAnalyzer: React.FC = () => {
  const { analyze, loading, error, results, setResults } = useCodeAnalysis();

  if (loading) {
    return <LoadingState />;
  }

  if (error) {
    return (
      <div className="h-full flex flex-col items-center justify-center p-12 text-center space-y-6 animate-in zoom-in-95 duration-300">
        <div className="w-20 h-20 bg-red-500/10 rounded-full flex items-center justify-center border border-red-500/20 shadow-[0_0_30px_rgba(239,68,68,0.2)]">
          <AlertCircle className="w-10 h-10 text-red-500" />
        </div>
        <div className="space-y-2">
          <h3 className="text-xl font-bold tracking-tight">Analysis Interrupted</h3>
          <p className="text-sm text-destructive font-medium bg-red-500/10 px-4 py-2 rounded-lg border border-red-500/20">
            {error}
          </p>
          <p className="text-xs text-muted-foreground max-w-sm mx-auto pt-4">
            This could be due to a network timeout or an issue with the AI engine. 
            Please check your connection and try again.
          </p>
        </div>
        <Button onClick={() => window.location.reload()} variant="outline" className="gap-2 px-8 rounded-full border-red-500/20 hover:bg-red-500/10">
          <RotateCcw className="w-4 h-4" />
          Restart System
        </Button>
      </div>
    );
  }

  if (results) {
    return <AnalysisResults results={results} onReset={() => setResults(null)} />;
  }

  return <CodeInput onAnalyze={analyze} isLoading={loading} />;
};
