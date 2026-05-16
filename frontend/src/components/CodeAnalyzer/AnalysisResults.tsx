import React from 'react';
import { motion } from 'framer-motion';
import { 
  CheckCircle2, 
  AlertTriangle, 
  ShieldAlert, 
  Zap, 
  ArrowRight, 
  RotateCcw,
  Clock,
  Code2,
  FileSearch,
  Lightbulb,
  Cpu
} from 'lucide-react';
import { Button, Card, Badge } from '../ui/core';

interface AnalysisResultsProps {
  results: any;
  onReset: () => void;
}

export const AnalysisResults: React.FC<AnalysisResultsProps> = ({ results, onReset }) => {
  const { summary, issues, betterApproach, optimizedCode, faangInsights } = results;

  return (
    <div className="space-y-6 animate-in slide-in-from-right-4 duration-500 pb-12">
      {/* 1. Summary Header */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card className="flex flex-col items-center justify-center p-6 text-center bg-primary/5 border-primary/20">
          <span className="text-[10px] uppercase tracking-widest text-muted-foreground mb-2">Quality Score</span>
          <div className="text-4xl font-black text-primary mb-1">{summary.score}/10</div>
          <Badge variant={summary.score > 7 ? 'success' : 'warning'} className="bg-primary/20 text-primary border-none">
            {summary.quality}
          </Badge>
        </Card>
        
        <Card className="flex flex-col items-center justify-center p-6 text-center">
          <Clock className="w-5 h-5 text-blue-400 mb-2" />
          <span className="text-[10px] uppercase tracking-widest text-muted-foreground mb-2">Time Complexity</span>
          <div className="text-lg font-bold">{summary.timeComplexity}</div>
        </Card>

        <Card className="flex flex-col items-center justify-center p-6 text-center">
          <Cpu className="w-5 h-5 text-purple-400 mb-2" />
          <span className="text-[10px] uppercase tracking-widest text-muted-foreground mb-2">Space Complexity</span>
          <div className="text-lg font-bold">{summary.spaceComplexity}</div>
        </Card>

        <Card className="flex flex-col items-center justify-center p-6 text-center">
          <div className="flex -space-x-2 mb-2">
            <ShieldAlert className="w-5 h-5 text-red-500" />
            <AlertTriangle className="w-5 h-5 text-yellow-500" />
          </div>
          <span className="text-[10px] uppercase tracking-widest text-muted-foreground mb-2">Issues Found</span>
          <div className="text-lg font-bold">{issues.length} Items</div>
        </Card>
      </div>

      {/* 2. Top-Level Summary Card */}
      <Card className="p-6 bg-secondary/10 border-white/5">
        <h4 className="text-xs font-bold uppercase tracking-widest text-muted-foreground mb-3">Analysis Summary</h4>
        <p className="text-sm leading-relaxed opacity-80">{summary.text}</p>
      </Card>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* 3. Issues List */}
        <div className="space-y-4">
          <h4 className="text-sm font-bold flex items-center gap-2 px-2">
            <FileSearch className="w-4 h-4 text-primary" />
            Detected Issues
          </h4>
          <div className="space-y-3">
            {issues.map((issue: any, idx: number) => (
              <motion.div
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: idx * 0.1 }}
                key={idx}
                className="p-4 rounded-xl bg-secondary/20 border border-white/5 flex gap-3 items-start group hover:bg-secondary/30 transition-all"
              >
                <div className="mt-1">
                  <AlertTriangle className="w-4 h-4 text-yellow-500" />
                </div>
                <div>
                  <p className="text-sm font-medium leading-tight">{issue.message}</p>
                  <span className="text-[10px] font-mono opacity-40 mt-1 block">Contextual Issue #{idx + 1}</span>
                </div>
              </motion.div>
            ))}
            {issues.length === 0 && (
              <div className="p-8 text-center bg-secondary/10 rounded-xl border border-dashed border-white/10 opacity-30">
                <CheckCircle2 className="w-8 h-8 mx-auto mb-2" />
                <p className="text-xs">Perfect execution - no issues found</p>
              </div>
            )}
          </div>
        </div>

        {/* 4. Better Approach & FAANG Insights */}
        <div className="space-y-6">
          {betterApproach && (
            <div className="space-y-4">
              <h4 className="text-sm font-bold flex items-center gap-2 px-2">
                <Zap className="w-4 h-4 text-yellow-500" />
                Better Approach
              </h4>
              <Card className="p-5 bg-yellow-500/5 border-yellow-500/10">
                <p className="text-sm leading-relaxed opacity-90">{betterApproach}</p>
              </Card>
            </div>
          )}

          {faangInsights && (
            <div className="space-y-4">
              <h4 className="text-sm font-bold flex items-center gap-2 px-2">
                <Lightbulb className="w-4 h-4 text-primary" />
                FAANG Level Insights
              </h4>
              <Card className="p-5 bg-primary/5 border-primary/10 border-l-4 border-l-primary">
                <p className="text-sm italic opacity-80 leading-relaxed">
                  "{faangInsights}"
                </p>
              </Card>
            </div>
          )}
        </div>
      </div>

      {/* 5. Optimized Code Block */}
      {optimizedCode && optimizedCode !== 'N/A' && (
        <Card className="p-0 overflow-hidden border-primary/20 bg-black/40">
          <div className="px-5 py-3 border-b border-white/5 bg-primary/10 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Code2 className="w-4 h-4 text-primary" />
              <span className="text-xs font-bold uppercase tracking-widest text-primary">Optimized Implementation</span>
            </div>
            <Button size="sm" variant="ghost" className="h-7 text-[10px] gap-1.5" onClick={() => navigator.clipboard.writeText(optimizedCode)}>
              Copy Code
            </Button>
          </div>
          <pre className="p-6 text-xs font-mono overflow-x-auto text-blue-300/90 leading-relaxed bg-[#0d0d0f]">
            <code>{optimizedCode}</code>
          </pre>
        </Card>
      )}

      {/* Actions */}
      <div className="flex justify-center pt-6">
        <Button onClick={onReset} variant="outline" className="gap-2 px-8 rounded-full">
          <RotateCcw className="w-4 h-4" />
          Review New Code
        </Button>
      </div>
    </div>
  );
};
